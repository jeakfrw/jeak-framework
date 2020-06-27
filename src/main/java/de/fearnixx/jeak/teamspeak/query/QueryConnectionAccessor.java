package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import de.fearnixx.jeak.teamspeak.except.QueryClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class QueryConnectionAccessor extends AbstractQueryConnection implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(QueryConnectionAccessor.class);

    @Inject
    private IInjectionService injectionService;

    @Inject
    private IEventService eventService;

    @Inject
    private IBot bot;

    private QueryEventDispatcher dispatcher;
    private TS3Connection connection;

    private boolean terminated = false;

    public void initialize(InputStream in, OutputStream out) {
        if (connection != null) {
            throw new IllegalStateException("Cannot re-run already used connection!");
        }

        dispatcher = injectionService.injectInto(new QueryEventDispatcher());
        eventService.registerListener(dispatcher);
        connection = new TS3Connection(in, out, this::onAnswer, this::onNotification);
        injectionService.injectInto(connection);
    }

    @Override
    public void run() {
        sendRequest(whoAmIRequest);

        try {
            read(connection);
        } catch (QueryClosedException e) {
            logger.info("Disconnected.");
            BotStateEvent.ConnectEvent.Disconnect disconnectEvent = new BotStateEvent.ConnectEvent.Disconnect(false);
            disconnectEvent.setBot(bot);
            eventService.fireEvent(disconnectEvent);
        }
    }

    private void read(TS3Connection connection) {
        while (!terminated) {
            try {
                connection.read();
            } catch (QueryClosedException e) {
                BotStateEvent.ConnectEvent.Disconnect disconnectEvent = new BotStateEvent.ConnectEvent.Disconnect(false);
                disconnectEvent.setBot(bot);
                eventService.fireEvent(disconnectEvent);
                return;

            } catch (IOException e) {
                logger.error("Failed to read from connection.", e);
                BotStateEvent.ConnectEvent.Disconnect disconnectEvent = new BotStateEvent.ConnectEvent.Disconnect(false);
                disconnectEvent.setBot(bot);
                eventService.fireEvent(disconnectEvent);
                return;

            } catch (ConsistencyViolationException e) {
                reportConsistencyViolation(e);

            } catch (Exception e) {
                logger.error("Fatal error occurred while reading the connection!", e);
            }
        }

        logger.info("Disconnected.");
        BotStateEvent.ConnectEvent.Disconnect disconnectEvent = new BotStateEvent.ConnectEvent.Disconnect(true);
        disconnectEvent.setBot(bot);
        eventService.fireEvent(disconnectEvent);
    }

    private void reportConsistencyViolation(ConsistencyViolationException e) {
        logger.warn("Consistency violation reported.", e);
    }

    private void onAnswer(RawQueryEvent.Message.Answer event) {
        event.setConnection(this);
        dispatcher.dispatchAnswer(event);
    }

    private void onNotification(RawQueryEvent.Message.Notification event) {
        event.setConnection(this);
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
            logger.warn("Error while trying to close connection.", e);
        }
    }

    @Override
    public boolean isClosed() {
        return connection != null && connection.isClosed();
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
