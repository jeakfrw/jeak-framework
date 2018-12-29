package de.fearnixx.t3.event;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.fearnixx.t3.Main;
import de.fearnixx.t3.T3Bot;
import de.fearnixx.t3.event.bot.IBotStateEvent;
import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.teamspeak.except.ConsistencyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * EventService used by the T3ServerBot
 * <p>
 * The event manager manages all events fired within an instance of the T3ServerBot.
 * Each bot creates it's own instances.
 * <p>
 * The following system properties are acknowledged by the EventService class
 * * "bot.eventmgr.poolsize" (Integer)
 * * "bot.eventmgr.terminatedelay" (Integer in milliseconds)
 */
public class EventService implements IEventService {

    public static final Integer THREAD_POOL_SIZE = 10;
    public static Integer AWAIT_TERMINATION_DELAY = 5000;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final Object LOCK = new Object();
    private final List<EventListenerContainer> containers =  new LinkedList<>();

    private final ExecutorService eventExecutor;

    private boolean terminated;

    public EventService() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("event-scheduler-%d").build();
        eventExecutor = Executors.newFixedThreadPool(Main.getProperty("bot.eventmgr.poolsize", THREAD_POOL_SIZE), threadFactory);
        AWAIT_TERMINATION_DELAY = Main.getProperty("bot.eventmgr.terminatedelay", AWAIT_TERMINATION_DELAY);
    }

    /**
     * Fires a specified event to all suitable listeners
     *
     * @param event {@link IEvent} instance
     */
    @Override
    public void fireEvent(IEvent event) {
        Objects.requireNonNull(event, "Event may not be null!");

        // Run on a temporary copy so adding new listeners during an event doesn't cause a dead-lock!
        final List<EventListenerContainer> acceptingListeners = new LinkedList<>();
        synchronized (LOCK) {
            if (terminated) return;
            containers.stream()
                    .filter(eventListenerContainer -> eventListenerContainer.accepts(event.getClass()))
                    .forEach(acceptingListeners::add);
        }

        // Sort by event order
        acceptingListeners.sort(Comparator.comparingInt(EventListenerContainer::getOrder));
        sendEvent(event, acceptingListeners);
    }

    private void sendEvent(IEvent event, List<EventListenerContainer> listeners) {
        logger.debug("Sending event: {} to {} listeners", event.getClass().getSimpleName(), listeners.size());

        if (isSynchronized(event)) {
            executeEvent(listeners, event);

        } else {
            // Execute using ThreadPoolExecutor
            eventExecutor.execute(() -> this.executeEvent(listeners, event));
        }
    }

    private boolean isSynchronized(IEvent event) {
        return event instanceof IBotStateEvent;
    }

    @SuppressWarnings("squid:S1193")
    private void executeEvent(List<EventListenerContainer> listeners, IEvent event) {
        for (int i = 0; i < listeners.size(); i++) {
            // Catch exceptions for each listener so a broken one doesn't break the whole event
            try {
                // Check if we got interrupted during processing
                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException("Interrupted during event execution");
                listeners.get(i).accept(event);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Interrupted event: {}! Processed {} out of {}",
                        event.getClass().getSimpleName(), i + 1, listeners.size(), e);
                return;

            } catch (EventAbortException abort) {
                // Event aborted!
                logger.warn("An event has been aborted: {}", abort.getMessage(), abort);
                return;

            } catch (ConsistencyViolationException e) {
                logger.error("An event listener has declared a consistency violation! We will abort the event execution.", e);
                return;

            } catch (Exception e) {
                // Skip the invocation exception for readability
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                logger.warn("Failed to pass event {} to {}", event.getClass().getSimpleName(), listeners.get(i).getVictim().getClass().toGenericString(), cause);

                if (!(e instanceof RuntimeException)) {
                    logger.error("In addition the last event listener threw a checked exception! Passing those is NOT allowed. We will unregister the listener!");
                    unregisterListener(listeners.get(i).getVictim());
                }
            }
        }
    }

    /**
     * Adds a new {@link EventListenerContainer} to the event listeners
     */
    public void addContainer(EventListenerContainer container) {
        synchronized (LOCK) {
            containers.add(container);
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
    public void registerListener(Object victim) {
        Objects.requireNonNull(victim, "Listener victim may not be null!");

        synchronized (LOCK) {
            for (Method method : victim.getClass().getMethods()) {
                Listener anno = method.getAnnotation(Listener.class);
                if (anno == null)
                    continue;

                containers.add(new EventListenerContainer(victim, method));
            }
        }
    }

    @Override
    public void unregisterListener(Object victim) {
        Objects.requireNonNull(victim, "Listener victim may not be null!");

        synchronized (LOCK) {
            for (Method method : victim.getClass().getMethods()) {
                Listener anno = method.getAnnotation(Listener.class);
                if (anno == null)
                    continue;

                containers.removeIf(container -> container.getVictim() == victim);
            }
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
                logger.error("Got interrupted while awaiting thread termination!", e);
                Thread.currentThread().interrupt();
            }
            if (!terminated_successfully) {
                logger.warn("Some events did not terminate gracefully! Either consider increasing the wait timeout or debug what plugin delays the shutdown!");
                logger.warn("Be aware that the JVM will not exit until ALL threads have terminated!");
            }
        }
    }
}
