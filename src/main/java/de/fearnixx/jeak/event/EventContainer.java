package de.fearnixx.jeak.event;

import de.fearnixx.jeak.event.except.EventAbortException;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventContainer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EventContainer.class);

    private long startedOn;
    private Thread worker;
    private int currentReceiverIndex = 0;
    private EventListenerContainer currentReceiver;
    private boolean receiverInterrupt = false;

    private final long scheduledOn;
    private final EventService eventService;
    private final List<EventListenerContainer> receivers;
    private final IEvent event;

    public EventContainer(EventService eventService, List<EventListenerContainer> receivers, IEvent event) {
        this.eventService = eventService;
        this.receivers = receivers;
        this.event = event;
        scheduledOn =  System.currentTimeMillis();
    }

    @Override
    public void run() {
        worker = Thread.currentThread();
        while (currentReceiverIndex < receivers.size()) {
            // Catch exceptions for each listener so a broken one doesn't break the whole event
            try {
                // Check if we got interrupted during processing

                EventListenerContainer container;
                synchronized (this) {
                    startedOn = System.currentTimeMillis();
                    currentReceiver = receivers.get(currentReceiverIndex);
                    container = currentReceiver;
                }

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException("Interrupted during event execution");
                container.accept(event);

            } catch (InterruptedException e) {
                synchronized (this) {
                    if (!receiverInterrupt) {
                        Thread.currentThread().interrupt();
                        logger.info("Interrupted event: {}! Processed {} out of {}",
                                event.getClass().getSimpleName(), currentReceiverIndex + 1, receivers.size(), e);
                    } else {
                        final long duration = getListenerRuntime() / 1000;
                        resetStartedOn();
                        receiverInterrupt = false;
                        logger.warn("Receiver interrupted forcefully after {} seconds. Listener: {}", duration, currentReceiver.getListenerFQN());
                    }
                }

            } catch (EventAbortException abort) {
                resetStartedOn();
                // Event aborted!
                logger.warn("An event has been aborted: {}", abort.getMessage(), abort);
                throw abort;

            } catch (ConsistencyViolationException e) {
                resetStartedOn();
                logger.error("An event listener has declared a consistency violation! We will abort the event execution.", e);
                return;

            } catch (RuntimeException e) {
                synchronized (this) {
                    resetStartedOn();
                    // Skip the invocation exception for readability
                    logExceptionCause(e);
                }

            } catch (Exception e) {
                synchronized (this) {
                    resetStartedOn();
                    // Skip the invocation exception for readability
                    logExceptionCause(e);
                    logger.error("In addition, the last event listener threw a checked exception! Passing those is NOT allowed. We will unregister the listener!");
                    eventService.unregisterListener(currentReceiver.getVictim());
                }
            }

            synchronized (this) {
                currentReceiverIndex++;
            }
        }
    }

    private void logExceptionCause(Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        logger.warn("Failed to pass event {} to {}", event.getClass().getSimpleName(), currentReceiver.getListenerFQN(), cause);
    }

    protected void resetStartedOn() {
        synchronized (this) {
            startedOn = 0;
        }
    }

    public synchronized void interruptReceiver() {
        receiverInterrupt = true;
        worker.interrupt();
    }

    public synchronized long getListenerRuntime() {
        return System.currentTimeMillis() - startedOn;
    }

    public List<EventListenerContainer> getListeners() {
        return receivers;
    }

    public IEvent getEvent() {
        return event;
    }

    public long getScheduledOn() {
        return scheduledOn;
    }
}
