package de.fearnixx.t3.event;

import de.fearnixx.t3.Main;
import de.fearnixx.t3.reflect.listeners.ListenerContainer;
import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * EventManager used by the T3ServerBot
 *
 * The event manager manages all events fired within an instance of the T3ServerBot.
 * Each bot creates it's own instances.
 *
 * The following system properties are acknowledged by the EventManager class
 * * "bot.eventmgr.poolsize" (Integer)
 * * "bot.eventmgr.terminatedelay" (Integer in milliseconds)
 */
public class EventManager implements IEventManager {

    public static final Integer THREAD_POOL_SIZE = 10;
    public static Integer AWAIT_TERMINATION_DELAY = 5000;

    private ILogReceiver log;
    private final List<ListenerContainer> addedContainers;
    private final List<ListenerContainer> listeners;
    private final ExecutorService eventExecutor;

    private boolean terminated;

    public EventManager(ILogReceiver log) {
        this.log = log;
        addedContainers = new ArrayList<>();
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
        synchronized (addedContainers) {
            if (terminated) return;

            final List<ListenerContainer> listeners2 = new ArrayList<>();
            listeners2.addAll(addedContainers);
            synchronized (listeners) {
                listeners2.addAll(listeners);
            }

            log.finest("Sending event: ", event.getClass().getSimpleName(), " to ", listeners2.size(), " listeners");
            // Execute using ThreadPoolExecutor
            eventExecutor.execute(() -> {
                for (int i = 0; i < listeners2.size(); i++) {
                    // Catch exceptions for each listener so a broken one doesn't break the whole event
                    try {
                        // Check if we got interrupted during processing
                        if (Thread.currentThread().isInterrupted())
                            throw new InterruptedException("Interrupted during event execution");
                        listeners2.get(i).fireEvent(event);
                    } catch (InterruptedException e) {
                        log.warning("Interrupted event: ", event.getClass().getSimpleName(), " assuming thread pool interruption! Processed ", i+1, " out of ", listeners2.size());
                        return;
                    } catch (Throwable e) {
                        // Skip the invocation exception for readability
                        if (e.getCause() != null) e = e.getCause();
                        log.severe("Failed to pass event ", event.getClass().getSimpleName(), " to ", listeners2.get(i).getVictim().getClass().toGenericString(), e);
                    }
                }
            });
        }
    }

    /**
     * Adds a new {@link ListenerContainer} to the event listeners
     */
    public void addContainer(ListenerContainer c) {
        synchronized (addedContainers) {
            addedContainers.add(c);
        }
    }

    /**
     * @see IEventManager#registerListeners(Object...)
     */
    @Override
    public void registerListeners(Object... l) {
        for (Object o : l)
            registerListener(o);
    }

    /**
     * @see IEventManager#registerListener(Object)
     */
    @Override
    public void registerListener(Object o) {
        synchronized (listeners) {
            if (listeners.stream().anyMatch(c -> c.getVictim() == o)) return;
            listeners.add(new ListenerContainer(o, log));
        }
    }

    /**
     * Attempts to shut down the EventManager and all running event tasks.
     * Waits for termination as long as specified by AWAIT_TERMINATION_DELAY
     */
    public void shutdown() {
        synchronized (addedContainers) {
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
