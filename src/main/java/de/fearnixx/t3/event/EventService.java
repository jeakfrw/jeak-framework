package de.fearnixx.t3.event;

import de.fearnixx.t3.Main;
import de.fearnixx.t3.event.bot.IBotStateEvent;
import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.reflect.SystemListener;
import de.fearnixx.t3.service.event.IEventService;
import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * EventService used by the T3ServerBot
 *
 * The event manager manages all events fired within an instance of the T3ServerBot.
 * Each bot creates it's own instances.
 *
 * The following system properties are acknowledged by the EventService class
 * * "bot.eventmgr.poolsize" (Integer)
 * * "bot.eventmgr.terminatedelay" (Integer in milliseconds)
 */
public class EventService implements IEventService {

    public static final Integer THREAD_POOL_SIZE = 10;
    public static Integer AWAIT_TERMINATION_DELAY = 5000;

    private ILogReceiver log;
    private final Object LOCK = new Object();
    private final List<EventListener> containers;
    private final List<EventListener> systemContainers;
    private final List<EventListener> listeners;
    private final List<EventListener> systemListeners;
    private final ExecutorService eventExecutor;

    private boolean terminated;

    public EventService(ILogReceiver log) {
        this.log = log;
        systemContainers = new ArrayList<>();
        systemListeners = new ArrayList<>();
        containers = new ArrayList<>();
        listeners = new ArrayList<>();
        terminated = false;

        eventExecutor = Executors.newFixedThreadPool(Main.getProperty("bot.eventmgr.poolsize", THREAD_POOL_SIZE));
        AWAIT_TERMINATION_DELAY = Main.getProperty("bot.eventmgr.terminatedelay", AWAIT_TERMINATION_DELAY);
    }

    /**
     * Fires a specified event to all suitable listeners
     * @param event {@link IEvent} instance
     */
    @Override
    public void fireEvent(IEvent event) {
        // Run on a temporary copy so adding new listeners during an event doesn't cause a dead-lock!
        final List<EventListener> listeners2 = new ArrayList<>();
        synchronized (LOCK) {
            if (terminated) return;
            listeners2.addAll(systemContainers);
            listeners2.addAll(systemListeners);
            listeners2.addAll(containers);
            listeners2.addAll(listeners);
        }
        sendEvent(event, listeners2);
    }

    protected void sendEvent(IEvent event, List<EventListener> listeners) {
        log.finest("Sending event: ", event.getClass().getSimpleName(), " to ", listeners.size(), " listeners");

        Runnable runnable = () -> {
            for (int i = 0; i < listeners.size(); i++) {
                // Catch exceptions for each listener so a broken one doesn't break the whole event
                try {
                    // Check if we got interrupted during processing
                    if (Thread.currentThread().isInterrupted())
                        throw new InterruptedException("Interrupted during event execution");
                    listeners.get(i).fireEvent(event);
                } catch (InterruptedException e) {
                    log.warning("Interrupted event: ", event.getClass().getSimpleName(), " ! Processed ", i + 1, " out of ", listeners.size(), e);
                    return;
                } catch (EventAbortException abort) {
                    // Event aborted!
                    log.severe("An event has been aborted!", abort);
                    return;
                } catch (Throwable e) {
                    // Skip the invocation exception for readability
                    if (e.getCause() != null) e = e.getCause();
                    log.severe("Failed to pass event ", event.getClass().getSimpleName(), " to ", listeners.get(i).getVictim().getClass().toGenericString(), e);
                }
            }
        };

        // The PostShutdown event shall not be called asynchronously
        if (event instanceof IBotStateEvent.IPostShutdown) {
            runnable.run();
        } else {
            // Execute using ThreadPoolExecutor
            eventExecutor.execute(runnable);
        }
    }

    /**
     * Adds a new {@link EventListener} to the event listeners
     */
    public void addContainer(EventListener c) {
        synchronized (LOCK) {
            containers.add(c);
        }
    }

    /**
     * Adds a new {@link EventListener} to the event listeners
     */
    public void addSystemContainer(EventListener c) {
        synchronized (LOCK) {
            containers.add(c);
        }
    }

    /**
     * @see IEventService#registerListeners(Object...)
     */
    @Override
    public void registerListeners(Object... l) {
        for (Object o : l)
            registerListener(o);
    }

    /**
     * @see IEventService#registerListener(Object)
     */
    @Override
    public void registerListener(Object o) {
        synchronized (LOCK) {
            if (listeners.stream().anyMatch(c -> c.getVictim() == o)
                || systemListeners.stream().anyMatch(c -> c.getVictim() == o)) return;
            EventListener listener = new EventListener(log, Listener.class, o);
            if (listener.hasAny())
                listeners.add(listener);
            listener = new EventListener(log, SystemListener.class, o);
            if (listener.hasAny())
                systemListeners.add(listener);
        }
    }

    /**
     * Attempts to shut down the EventService and all running event tasks.
     * Waits for termination as long as specified by AWAIT_TERMINATION_DELAY
     */
    public void shutdown() {
        synchronized (LOCK) {
            terminated = true;
            boolean terminated_successfully = false;
            try {
                eventExecutor.shutdownNow();
                terminated_successfully = eventExecutor.awaitTermination(AWAIT_TERMINATION_DELAY, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.severe("Got interrupted while awaiting thread termination!", e);
            }
            if (!terminated_successfully) {
                log.warning("Some events did not terminate gracefully! Either consider increasing the wait timeout or debug what plugin delays the shutdown!");
                log.warning("Be aware that the JVM will not exit until ALL threads have terminated!");
            }
        }
    }
}
