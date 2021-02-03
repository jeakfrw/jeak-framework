package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.event.query.RawQueryEvent.Message;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.api.ITSMessageChannel;
import de.fearnixx.jeak.teamspeak.query.api.ITSQueryConnection;
import de.fearnixx.jeak.util.URIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TSQueryConnection implements ITSQueryConnection, Runnable {

    public static final IQueryRequest KEEPALIVE_REQUEST = IQueryRequest.builder().command(QueryCommands.WHOAMI).build();
    public static final int KEEP_ALIVE_SECS = Main.getProperty("bot.connection.keepalive", 240);
    public static final int KEEP_ALIVE_MILLIS = KEEP_ALIVE_SECS * 1000;
    public static final long READ_TIMEOUT_MILLIS = KEEP_ALIVE_MILLIS * 2L;
    private static final float MAX_REQUEST_INTERVAL_PER_SECOND = Main.getProperty("bot.connection.reqdelay", 0.25f);
    private static final float MS_DELAY_PER_MESSAGE = 1000 * MAX_REQUEST_INTERVAL_PER_SECOND;
    private static final int WAIT_INTERVAL = Main.getProperty("bot.connection.wait_interval_ms", 50);
    private static final Logger logger = LoggerFactory.getLogger(TSQueryConnection.class);

    private URIContainer createdWithURI;
    private final ITSMessageChannel messageChannel;
    private final StandardMessageMarshaller marshaller;
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    private final Thread serialChannelHost;
    private final Queue<IQueryRequest> requestQueue = new LinkedList<>();
    private final List<Consumer<IQueryEvent.INotification>> notificationListeners = new ArrayList<>();
    private final List<Consumer<IQueryEvent.IAnswer>> answerListeners = new ArrayList<>();
    private final List<BiConsumer<ITSQueryConnection, Boolean>> closeListeners = new ArrayList<>();
    private final AtomicReference<String> lockListenersReason = new AtomicReference<>();
    private final AtomicLong lastRequestTSP = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong lastReceivedTSP = new AtomicLong(0);
    private final AtomicBoolean gracefullyClosed = new AtomicBoolean(false);

    public TSQueryConnection(ITSMessageChannel messageChannel, StandardMessageMarshaller marshaller) {
        this.messageChannel = messageChannel;
        this.marshaller = marshaller;
        this.messageChannel.setNotificationCallback(this::dispatchNotification);
        this.messageChannel.setAnswerCallback(this::dispatchAnswer);
        this.serialChannelHost = new Thread(messageChannel);
    }

    public void setURI(URIContainer uri) {
        this.createdWithURI = uri;
    }

    public URIContainer getURI() {
        return createdWithURI;
    }

    @Override
    public void queueRequest(IQueryRequest queryRequest) {
        assertUnterminated();
        synchronized (requestQueue) {
            requestQueue.add(queryRequest);
        }
    }

    @Override
    public Future<IQueryEvent.IAnswer> promiseRequest(IQueryRequest queryRequest) {
        assertUnterminated();
        final var future = new CompletableFuture<IQueryEvent.IAnswer>();
        queryRequest.onDone(future::complete);
        synchronized (requestQueue) {
            requestQueue.add(queryRequest);
        }
        return future;
    }

    @Override
    public synchronized void onNotification(Consumer<IQueryEvent.INotification> notificationConsumer) {
        assertListenersUnlocked();
        Objects.requireNonNull(notificationConsumer, "Notification listener may not be null!");
        notificationListeners.add(notificationConsumer);
    }

    @Override
    public synchronized void onAnswer(Consumer<IQueryEvent.IAnswer> answerConsumer) {
        assertListenersUnlocked();
        Objects.requireNonNull(answerConsumer, "Answer listener may not be null!");
        answerListeners.add(answerConsumer);
    }

    @Override
    public synchronized void onClosed(BiConsumer<ITSQueryConnection, Boolean> closeConsumer) {
        assertListenersUnlocked();
        Objects.requireNonNull(closeConsumer, "Close listener may not be null!");
        closeListeners.add(closeConsumer);
    }

    protected synchronized void assertListenersUnlocked() {
        final String reason = lockListenersReason.get();
        if (reason != null) {
            throw new IllegalStateException("Listeners locked: " + reason);
        }
    }

    @Override
    public boolean isActive() {
        synchronized (this) {
            return messageChannel.isOpen();
        }
    }

    @Override
    public synchronized void shutdown() {
        uncheckedClose(true);
    }

    @Override
    public synchronized void close() throws Exception {
        if (messageChannel.isOpen()) {
            messageChannel.close();
        }
    }

    @Override
    public synchronized void lockListeners(String reason) {
        assertListenersUnlocked();
        lockListenersReason.set(reason);
    }

    protected void uncheckedClose(boolean graceful) {
        try {
            close();
            gracefullyClosed.set(graceful);
        } catch (Exception e) {
            logger.error("Failed to close connection!", e);
        }
    }

    @Override
    public void run() {
        doStartup();

        // Active run loop
        doLifeCycle();

        // Connection finished - perform cleanup.
        doCleanup();
    }

    protected synchronized void doStartup() {
        if (serialChannelHost.isAlive()) {
            throw new IllegalStateException("Already connected?!");
        }
        serialChannelHost.start();
        lastReceivedTSP.set(System.currentTimeMillis());
    }

    protected void doLifeCycle() {
        while (isActive() && !Thread.currentThread().isInterrupted()) {
            sendIfPossible();
            checkTimeout();

            synchronized (this) {
                try {
                    this.wait(WAIT_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    protected void sendIfPossible() {
        synchronized (requestQueue) {
            if (!messageChannel.isReady() || messageChannel.hasPendingRequest()) {
                return;
            }

            boolean maySend = MS_DELAY_PER_MESSAGE == 0
                    || (System.currentTimeMillis() - lastRequestTSP.get()) > MS_DELAY_PER_MESSAGE;
            if (!maySend) {
                return;
            }

            final var request = requestQueue.poll();
            if (request != null) {
                messageChannel.writeMessage(request);
                lastRequestTSP.set(System.currentTimeMillis());
            }
        }
    }

    protected synchronized void checkTimeout() {
        final long silence = System.currentTimeMillis() - lastReceivedTSP.get();
        if (silence > READ_TIMEOUT_MILLIS) {
            logger.error("Read timed out. Closing connection.");
            uncheckedClose(false);
        } else if (silence > KEEP_ALIVE_MILLIS && !messageChannel.hasPendingRequest()) {
            queueRequest(KEEPALIVE_REQUEST);
        }
    }

    protected synchronized void doCleanup() {
        logger.debug("Performing cleanup.");
        terminated.set(true);

        // Empty request queue and abort messages.
        synchronized (requestQueue) {
            requestQueue.removeIf(req -> {
                logger.debug("Rejecting queued request: {}", req.getCommand());
                final var error = new RawQueryEvent.ErrorMessage(req);
                error.setNext(error);
                error.setProperty("id", "-1");
                error.setProperty("msg", "Connection closed.");
                error.setConnection((IQueryConnection) this);

                final var answer = new RawQueryEvent.Message.Answer(req);
                answer.setConnection((IQueryConnection) this);
                answer.setError(error);
                dispatchAnswer(answer);
                return true;
            });
            final boolean gracefulState = gracefullyClosed.get();
            closeListeners.forEach(it -> it.accept(this, gracefulState));
        }

        // Close connection
        uncheckedClose(gracefullyClosed.get());
    }

    protected void dispatchAnswer(Message.Answer message) {
        final var marshalled = marshaller.marshall(message);
        synchronized (this) {
            lastReceivedTSP.set(System.currentTimeMillis());
            this.answerListeners.forEach(it -> it.accept(marshalled));
        }
    }

    protected void dispatchNotification(Message.Notification notification) {
        synchronized (this) {
            lastReceivedTSP.set(System.currentTimeMillis());
        }
        marshaller.marshall(notification)
                .forEach(marshalled -> {
                    synchronized (this) {
                        notificationListeners.forEach(it -> it.accept(marshalled));
                    }
                });
    }

    protected synchronized void assertUnterminated() {
        if (terminated.get()) {
            throw new IllegalStateException("Cannot perform action on terminated connection!");
        }
    }
}
