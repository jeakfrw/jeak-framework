package de.fearnixx.jeak.event;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.event.except.EventAbortException;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.util.NamePatternThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * <p>
 * The event manager manages all events fired within an instance of the bot.
 * Each bot creates it's own instances.
 * <p>
 * The following system properties are acknowledged by the EventService class
 * * "bot.eventmgr.poolsize" (Integer)
 * * "bot.eventmgr.terminatedelay" (Integer in milliseconds)
 */
public class EventService implements IEventService {

    public static final Integer THREAD_POOL_SIZE = Main.getProperty("jeak.eventmgr.poolsize", 10);
    public static final Integer AWAIT_TERMINATION_DELAY = Main.getProperty("jeak.eventmgr.terminatedelay", 10000);
    private static final boolean ENABLE_LISTENER_INTERRUPT = Main.getProperty("jeak.eventmgr.interruptListeners", true);

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final Object LOCK = new Object();
    private final List<EventListenerContainer> registeredListeners = new LinkedList<>();
    private final List<EventContainer> runningEvents = new ArrayList<>();
    private final ThreadPoolExecutor eventExecutor;
    private final ExecutorService deadCheckExecutor =
            Executors.newSingleThreadExecutor(new NamePatternThreadFactory("tasksvc-deadcheck-%d"));

    private boolean terminated;

    public EventService() {
        ThreadFactory threadFactory = new NamePatternThreadFactory("event-scheduler-%d");
        eventExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory);
        deadCheckExecutor.execute(this::deadListenerCheck);
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
            registeredListeners.stream()
                    .filter(container -> container.accepts(event.getClass()))
                    .forEach(acceptingListeners::add);
        }

        // Sort by event order
        acceptingListeners.sort(Comparator.comparingInt(EventListenerContainer::getOrder));
        sendEvent(new EventContainer(this, acceptingListeners, event));
    }

    private void sendEvent(final EventContainer container) {
        final String eventName = container.getEvent().getClass().getSimpleName();
        if (isSynchronized(container.getEvent())) {
            logger.debug("Sending event: {} to {} listeners", eventName, container.getListeners().size());
            container.run();

        } else {
            // Execute using ThreadPoolExecutor
            logger.debug("Queueing event {}", eventName);
            eventExecutor.execute(() -> {
                logger.debug("Sending event: {} to {} listeners", eventName, container.getListeners().size());
                synchronized (runningEvents) {
                    runningEvents.add(container);
                }
                try {
                    container.run();
                } catch (EventAbortException e) {
                    logger.debug("Aborted event: {}", eventName);
                }
                synchronized (runningEvents) {
                    runningEvents.remove(container);
                }
                logger.debug("Finished executing listeners for: {}", eventName);
            });
        }
    }

    private boolean isSynchronized(IEvent event) {
        return event instanceof IBotStateEvent;
    }

    /**
     * Adds a new {@link EventListenerContainer} to the event listeners
     */
    public void addContainer(EventListenerContainer container) {
        synchronized (LOCK) {
            registeredListeners.add(container);
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

                registeredListeners.add(new EventListenerContainer(victim, method));
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

                registeredListeners.removeIf(container -> container.getVictim() == victim);
            }
        }
    }

    @SuppressWarnings("squid:S2142")
    private void deadListenerCheck() {
        while (true) {
            synchronized (LOCK) {
                if (terminated) return;
            }

            synchronized (runningEvents) {
                runningEvents.stream()
                        .filter(container -> container.getListenerRuntime() > 10000 && ENABLE_LISTENER_INTERRUPT)
                        .forEach(container -> {
                            logger.debug("Attempting to interrupt listener for event: {}", container.getEvent().getClass().getSimpleName());
                            container.interruptReceiver();
                        });
            }

            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                logger.warn("Dead-Listener check interrupted.");
                return;
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
                eventExecutor.shutdown();
                deadCheckExecutor.shutdown();
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
