package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import static de.fearnixx.jeak.event.IRawQueryEvent.IMessage;

public class TS3Connection implements AutoCloseable {

    public static final int SOCKET_TIMEOUT_MILLIS = Main.getProperty("bot.connection.sotimeout", 500);
    public static final int KEEP_ALIVE_SECS = Main.getProperty("bot.connection.keepalive", 240);
    public static final int KEEP_ALIVE_MILLIS = KEEP_ALIVE_SECS * 1000;
    public static final int MAX_FAILING_KEEP_ALIVE = Main.getProperty("bot.connection.max_keepalive", 1);
    public static final float REQ_DELAY_SOT_FACTOR = Main.getProperty("bot.connection.reqdelay", 0.25f);
    public static final int REQUEST_DELAY_MILLIS = (int) Math.ceil(SOCKET_TIMEOUT_MILLIS * REQ_DELAY_SOT_FACTOR);

    private static final Logger logger = LoggerFactory.getLogger(TS3Connection.class);

    private final QueryMessageReader messageReader;
    private final QueryMessageWriter messageWriter;

    private final Consumer<IMessage.IAnswer> onAnswer;
    private final Consumer<IMessage.INotification> onNotification;

    private IQueryRequest currentRequest = IQueryRequest.builder().command("dummy").build();
    private final Queue<IQueryRequest> requestQueue = new LinkedList<>();

    private int requestDelayMillis = 0;
    private int timeoutCount = 0;
    private int keepAliveCount = 0;
    private final IQueryRequest keepAliveRequest =
            IQueryRequest.builder()
                    .command("version")
                    .onDone(event -> {
                        synchronized (requestQueue) {
                            timeoutCount = 0;
                            keepAliveCount = 0;
                        }
                    })
                    .build();

    public TS3Connection(InputStream in, OutputStream out, Consumer<IMessage.IAnswer> onAnswer, Consumer<IMessage.INotification> onNotification) {
        messageReader = new QueryMessageReader(in, this::onNotification, this::onAnswer, this::onGreetingStatus, this::supplyRequest);
        messageWriter = new QueryMessageWriter(out);
        this.onAnswer = onAnswer;
        this.onNotification = onNotification;
    }

    public void read() throws IOException {
        while (!messageReader.isClosed()) {
            try {
                nextRequest();
                messageReader.read();
            } catch (SocketTimeoutException timeout) {
                handleTimeout();
            }
        }
    }

    private void nextRequest() throws IOException {
        synchronized (requestQueue) {
            if (currentRequest == null && requestQueue.peek() != null && requestDelayMillis <= 0) {
                requestDelayMillis = REQUEST_DELAY_MILLIS;
                currentRequest = requestQueue.poll();
                messageWriter.writeMessage(currentRequest);
            }
        }
    }

    private void handleTimeout() throws IOException {
        synchronized (requestQueue) {
            if (requestDelayMillis > 0) {
                requestDelayMillis = requestDelayMillis - SOCKET_TIMEOUT_MILLIS;
            }

            int timeoutTime = ++timeoutCount * SOCKET_TIMEOUT_MILLIS;
            if (timeoutTime >= KEEP_ALIVE_MILLIS) {
                if (keepAliveCount++ > MAX_FAILING_KEEP_ALIVE) {
                    logger.error("Connection lost - Read timed out");
                    close();

                } else if (currentRequest == null){
                    logger.debug("Sending keepalive");
                    keepAliveCount = 0;
                    requestQueue.add(keepAliveRequest);
                }
            }
        }

    }

    protected void sendRequest(IQueryRequest request) {
        synchronized (requestQueue) {
            requestQueue.add(request);
        }
    }

    private IQueryRequest supplyRequest() {
        synchronized (requestQueue) {
            return currentRequest;
        }
    }

    private void onAnswer(IMessage.IAnswer event) {
        synchronized (requestQueue) {
            currentRequest = null;
        }
        
        if (onAnswer != null) {
            onAnswer.accept(event);
        }
    }

    private void onNotification(IMessage.INotification event) {
        if (onNotification != null) {
            onNotification.accept(event);
        }
    }

    private void onGreetingStatus(Boolean fullyReceived) {
        if (fullyReceived) {
            synchronized (requestQueue) {
                if (currentRequest != null && currentRequest.getCommand().equals("dummy")) {
                    currentRequest = null;
                } else {
                    throw new IllegalStateException("Greeting status may only be received initially!");
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        messageReader.close();
        messageWriter.close();
    }

    public boolean isClosed() {
        return messageReader.isClosed();
    }
}
