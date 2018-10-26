package de.fearnixx.t3.teamspeak.query;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import static de.fearnixx.t3.event.query.RawQueryEvent.Message;

public class TS3Connection {

    private QueryMessageReader messageReader;
    private QueryMessageWriter messageWriter;

    private QueryNotifier notifier = new QueryNotifier();
    private IQueryRequest currentRequest;
    private final LinkedList<IQueryRequest> requestQueue = new LinkedList<>();

    public TS3Connection(InputStream in, OutputStream out) {
        messageReader = new QueryMessageReader(in, this::onNotification, this::onAnswer);
        messageWriter = new QueryMessageWriter(out);
    }

    protected void sendRequest(IQueryRequest request) {

    }

    private void onAnswer(Message event) {

    }

    private void onNotification(Message event) {

    }
}
