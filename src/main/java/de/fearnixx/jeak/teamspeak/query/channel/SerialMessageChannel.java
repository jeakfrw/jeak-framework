package de.fearnixx.jeak.teamspeak.query.channel;

import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.api.ITSMessageChannel;
import de.fearnixx.jeak.teamspeak.query.api.ITSParser;
import de.fearnixx.jeak.teamspeak.query.api.QuerySyntaxException;
import de.fearnixx.jeak.teamspeak.query.parser.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static de.fearnixx.jeak.event.query.RawQueryEvent.Message;

public class SerialMessageChannel implements ITSMessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(SerialMessageChannel.class);
    private static final Logger netLogger = LoggerFactory.getLogger("de.fearnixx.jeak.teamspeak.query.Netlog");

    private final ByteChannel serialChannel;
    private final ITSParser parser = new QueryParser(this::getCurrentRequest);

    private final AtomicBoolean greetingState = new AtomicBoolean();
    private final AtomicReference<IQueryRequest> pendingRequest = new AtomicReference<>();
    private final AtomicReference<String> lastWrittenMessage = new AtomicReference<>();
    private final AtomicReference<Consumer<Message.Answer>> answerConsumer = new AtomicReference<>();
    private final AtomicReference<Consumer<Message.Notification>> notificationConsumer = new AtomicReference<>();
    private final AtomicReference<Consumer<IQueryRequest>> rejectedMessageConsumer = new AtomicReference<>();

    public SerialMessageChannel(ByteChannel serialChannel) {
        Objects.requireNonNull(serialChannel, "Underlying channel may not be null!");
        this.serialChannel = serialChannel;
        parser.setOnGreetingCallback(this::onGreetingReceived);
        parser.setOnAnswerCallback(this::onAnswerParsed);
        parser.setOnNotificationCallback(this::onNotificationParsed);
    }

    @Override
    public synchronized boolean isReady() {
        return greetingState.get();
    }

    protected synchronized IQueryRequest getCurrentRequest() {
        return pendingRequest.get();
    }

    @Override
    public synchronized boolean hasPendingRequest() {
        return pendingRequest.get() != null;
    }

    @Override
    public void writeMessage(IQueryRequest message) {
        synchronized (this) {
            if (!isReady()) {
                throw new IllegalStateException("Channel is not ready to receive requests yet!");
            }
            if (hasPendingRequest()) {
                throw new IllegalStateException("Channel blocked by pending request!");
            }
            pendingRequest.set(message);
        }

        final String withoutSeparator = QueryMessageSerializer.serialize(message);
        final String msg = withoutSeparator + System.lineSeparator();
        if (netLogger.isDebugEnabled()) {
            netLogger.debug("==> {}", withoutSeparator);
        }

        try {
            synchronized (this) {
                lastWrittenMessage.set(withoutSeparator);
                final int count = serialChannel.write(
                        ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8))
                );
                logger.debug("Wrote {} bytes to the channel.", count);
            }
        } catch (IOException e) {
            onMessageRejected(message);
            logger.error("Failed to write message to byte channel!", e);
        }
    }

    @Override
    public void run() {
        try (final var reader = new BufferedReader(Channels.newReader(serialChannel, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                receivedLine(line);
            }
        } catch (AsynchronousCloseException e) {
            logger.info("Channel closed async.");
        } catch (IOException e) {
            logger.error("Failed to read from byte channel!", e);
        }
        logger.debug("Channel finished.");
    }

    protected void receivedLine(String line) {
        if (line == null || line.isBlank()) {
            logger.debug("Skipping blank line.");
            return;
        }

        // Strip LF
        final int lastIdx = line.length() - 1;
        if (line.charAt(lastIdx) == '\n') {
            line = line.substring(0, lastIdx);
        }

        netLogger.debug("<== {}", line);
        try {
            parser.parseLine(line);
        } catch (QuerySyntaxException e) {
            logger.warn("Syntax exception while passing message!", e);
            synchronized (this) {
                if (!greetingState.get()) {
                    suppressedClose();
                }
            }
        }
    }

    protected void onNotificationParsed(Message.Notification notification) {
        final var cb = notificationConsumer.get();
        if (cb != null) {
            cb.accept(notification);
        }
    }

    protected void onAnswerParsed(Message.Answer answer) {
        synchronized (this) {
            pendingRequest.set(null);
        }
        final var cb = answerConsumer.get();
        if (cb != null) {
            cb.accept(answer);
        }
    }

    protected void onMessageRejected(IQueryRequest request) {
        final var cb = rejectedMessageConsumer.get();
        if (cb != null) {
            cb.accept(request);
        }
        synchronized (this) {
            pendingRequest.set(null);
        }
    }

    protected synchronized void onGreetingReceived(boolean greetingComplete) {
        greetingState.set(greetingComplete);
    }

    @Override
    public void setAnswerCallback(Consumer<Message.Answer> answerConsumer) {
        this.answerConsumer.set(answerConsumer);
    }

    @Override
    public void setNotificationCallback(Consumer<Message.Notification> notificationConsumer) {
        this.notificationConsumer.set(notificationConsumer);
    }

    @Override
    public void setRejectedMessageCallback(Consumer<IQueryRequest> rejectedMessageConsumer) {
        this.rejectedMessageConsumer.set(rejectedMessageConsumer);
    }

    @Override
    public boolean isOpen() {
        return serialChannel.isOpen();
    }

    protected void suppressedClose() {
        try {
            if (isOpen()) {
                close();
            }
        } catch (IOException e) {
            logger.debug("Failed to self-close!", e);
        }
    }

    @Override
    public void close() throws IOException {
        serialChannel.close();
    }
}
