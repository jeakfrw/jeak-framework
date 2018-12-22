package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.reflect.IInjectionService;
import de.fearnixx.t3.reflect.Inject;
import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class QueryConnectionAccessor extends AbstractQueryConnection implements Runnable {

    @Inject
    public ILogReceiver receiver;

    @Inject
    public IInjectionService injectionService;

    private QueryEventDispatcher dispatcher;
    private TS3Connection connection;

    private boolean terminated = false;

    public void initialize(InputStream in, OutputStream out) {
        if (connection != null) {
            throw new IllegalStateException("Cannot re-run already used connection!");
        }

        dispatcher = injectionService.injectInto(new QueryEventDispatcher());
        connection = new TS3Connection(in, out, this::onAnswer, this::onNotification);
        injectionService.injectInto(connection);
    }

    @Override
    public void run() {
        sendRequest(whoAmIRequest);

        try {
            read(connection);
        } catch (Exception e) {
            receiver.severe("Fatal error occurred while reading the connection!", e);
        }
    }

    private void read(TS3Connection connection) {
        while (!terminated && !connection.isClosed()) {
            try {
                connection.read();
            } catch (IOException e) {
                receiver.severe("Failed to read from connection.", e);
                return;
            }
        }
    }

    private void onAnswer(IRawQueryEvent.IMessage.IAnswer event) {
        dispatcher.dispatchAnswer(event);
    }

    private void onNotification(IRawQueryEvent.IMessage.INotification event) {
        dispatcher.dispatchNotification(event);
    }

    @Override
    public void sendRequest(IQueryRequest request) {
        connection.sendRequest(request);
    }

    public void shutdown() {
        try {
            terminated = true;
            connection.close();
        } catch (IOException e) {
            receiver.warning("Error while trying to close connection.", e);
        }
    }
}
