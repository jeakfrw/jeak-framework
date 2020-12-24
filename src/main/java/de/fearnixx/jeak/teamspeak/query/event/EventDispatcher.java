package de.fearnixx.jeak.teamspeak.query.event;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.event.IEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);
    private final ExecutorService eventExecutor = Executors.newFixedThreadPool(4);

    @Inject
    private IEventService eventService;

    public void dispatchAnswer(IQueryEvent.IAnswer answer) {
        eventExecutor.execute(() -> {
            if (answer.getErrorCode() == 0) {
                isolateExcept(() -> {
                    final var successConsumer = answer.getRequest().onSuccess();
                    if (successConsumer != null) {
                        successConsumer.accept(answer);
                    }
                });
            } else {
                isolateExcept(() -> {
                    final var errorConsumer = answer.getRequest().onError();
                    if (errorConsumer != null) {
                        errorConsumer.accept(answer);
                    }
                });
            }
            isolateExcept(() -> {
                final var doneConsumer = answer.getRequest().onDone();
                if (doneConsumer != null) {
                    doneConsumer.accept(answer);
                }
            });
            eventService.fireEvent(answer);
        });
    }

    protected void isolateExcept(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.warn("Uncaught exception in request callback!", e);
        }
    }

    public void dispatchNotification(IQueryEvent.INotification notification) {
        eventService.fireEvent(notification);
    }
}
