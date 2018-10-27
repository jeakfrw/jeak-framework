package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import static de.fearnixx.t3.event.IRawQueryEvent.IMessage;

public class TS3Connection implements AutoCloseable {

    public static final int SOCKET_TIMEOUT_MILLIS = Main.getProperty("bot.connection.sotimeout", 500);
    public static final int KEEP_ALIVE_SECS = Main.getProperty("bot.connection.keepalive", 240);
    public static final int KEEP_ALIVE_MILLIS = KEEP_ALIVE_SECS * 1000;
    public static final int MAX_FAILING_KEEPALIVE = Main.getProperty("bot.connection.max_keepalive", 1);
    public static final float REQ_DELAY_SOT_FACTOR = Main.getProperty("bot.connection.reqdelay", 0.25f);
    public static final int REQUEST_DELAY_MILLIS = (int) Math.ceil(SOCKET_TIMEOUT_MILLIS * REQ_DELAY_SOT_FACTOR);

    private final QueryMessageReader messageReader;
    private final QueryMessageWriter messageWriter;

    private final Consumer<IMessage.IAnswer> onAnswer;
    private final Consumer<IMessage.INotification> onNotification;

    private IQueryRequest currentRequest;
    private final Queue<IQueryRequest> requestQueue = new LinkedList<>();

    private int requestDelayMillis = 0;
    private int timeoutCount = 0;
    private int keepAliveCount = 0;
    private final IQueryRequest timeoutRequest =
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
        messageReader = new QueryMessageReader(in, this::onNotification, this::onAnswer, this::supplyRequest);
        messageWriter = new QueryMessageWriter(out);
        this.onAnswer = onAnswer;
        this.onNotification = onNotification;
    }

    public void read() throws IOException {
        try {
            messageReader.read();
        } catch (SocketTimeoutException timeout) {
            handleTimeout();
        }
    }

    private void handleTimeout() throws IOException {
        synchronized (requestQueue) {
            if (requestDelayMillis > 0)
                requestDelayMillis = requestDelayMillis - SOCKET_TIMEOUT_MILLIS;
        }

        int timeoutTime = ++timeoutCount * SOCKET_TIMEOUT_MILLIS;
        if (timeoutTime >= KEEP_ALIVE_MILLIS) {
            if (keepAliveCount++ > MAX_FAILING_KEEPALIVE) {
                log.severe("Connection lost - Read timed out");
                close();

            } else {
                log.finest("Sending keepalive");
                keepAliveCount = 0;
                sendRequest(timeoutRequest);
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
        if (onAnswer != null) {
            onAnswer.accept(event);
        }
    }

    private void onNotification(IMessage.INotification event) {
        if (onNotification != null) {
            onNotification.accept(event);
        }
    }

    @Override
    public void close() throws IOException {
        messageReader.close();
        messageWriter.close();
    }
}
