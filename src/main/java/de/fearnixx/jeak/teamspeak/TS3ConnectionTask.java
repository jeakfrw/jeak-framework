package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.except.QueryConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TS3ConnectionTask implements ITask {

    // Reconnection delay in seconds
    private static final Integer RECONNECT_DELAY = Main.getProperty("jeak.connection.reconnectDelay", 10);
    // Max tries (< 0 to disable)
    private static final Integer MAX_RECONNECT_TRIES = Main.getProperty("jeak.connection.reconnectTries", 10);

    private static final Logger logger = LoggerFactory.getLogger(TS3ConnectionTask.class);

    @Inject
    private IEventService eventService;

    @Inject
    private ITaskService taskService;

    @Inject
    private IServer server;

    @Inject
    private IBot bot;

    private final AtomicInteger failedAttempts = new AtomicInteger();
    private final AtomicBoolean trying = new AtomicBoolean();

    @Override
    public long getDelay() {
        return 0;
    }

    @Override
    public long getInterval() {
        return RECONNECT_DELAY;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public TaskType getType() {
        return TaskType.REPEAT;
    }

    @Override
    public Runnable getRunnable() {
        return () -> {
            if (!trying.get() && !server.isConnected()) {
                trying.set(true);
                logger.debug("Trying to reconnect to TS3...");
                try {
                    server.connect();
                } catch (QueryConnectException e) {
                    logger.debug("Failed to re-connect. Try: {} of {}", failedAttempts.get(), MAX_RECONNECT_TRIES, e);
                    fail();
                }

            }
        };
    }

    @Override
    public boolean shouldReschedule() {
        return !server.isConnected();
    }

    private void fail() {
        failedAttempts.incrementAndGet();
        IBotStateEvent event = new BotStateEvent.ConnectEvent.ConnectFailed();
        eventService.fireEvent(event);
        trying.set(false);
    }

    @Override
    public String getName() {
        return "frw-connect";
    }

    @Listener(order = Listener.Orders.LATEST)
    public void onDisconnect(IBotStateEvent.IConnectStateEvent.IDisconnect event) {
        if (event.isGraceful()) {
            return;
        }

        if (MAX_RECONNECT_TRIES != 0) {
            logger.info("Connection lost. Scheduling reconnect.");
            taskService.scheduleTask(this);
        } else {
            logger.warn("Connection lost. Shutting down.");
            bot.shutdown();
        }
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onPreConnectFailed(IBotStateEvent.IConnectStateEvent.IConnectFailed event) {
        BotStateEvent.ConnectEvent.ConnectFailed theEvent = ((BotStateEvent.ConnectEvent.ConnectFailed) event);
        theEvent.setAttempts(failedAttempts.incrementAndGet());
        theEvent.setMaxAttempts(MAX_RECONNECT_TRIES);
    }

    @Listener(order = Listener.Orders.LATEST)
    public void onConnectFailed(IBotStateEvent.IConnectStateEvent.IConnectFailed event) {
        if (event.maxAttempts() >= 0 && event.attemptCount() >= MAX_RECONNECT_TRIES) {
            logger.error("Failed to reconnect to TS3 in {} tries. Giving up...", MAX_RECONNECT_TRIES);
            taskService.removeTask(this);
            bot.shutdown();
        }
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        trying.set(false);
        taskService.removeTask(this);
        failedAttempts.set(0);
    }
}
