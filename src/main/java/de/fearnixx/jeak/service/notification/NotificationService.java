package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class NotificationService implements INotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final List<INotificationChannel> channels = new LinkedList<>();

    @Inject
    public ITaskService taskService;

    @Override
    public void dispatch(INotification notification) {
        Objects.requireNonNull(notification, "Notification may not be null!");

        // Queue for asynchronous processing.
        taskService.runTask(
                ITask.builder()
                        .name("NotificationDispatcher")
                        .runnable(() -> internalDispatch(notification))
                        .build()
        );
    }

    @Override
    public void registerChannel(INotificationChannel channel) {
        Objects.requireNonNull(channel, "Notification channel may not be null!");

        synchronized (channels) {
            if (!channels.contains(channel)) {
                channels.add(channel);
            }
        }
    }

    private void internalDispatch(INotification notification) {
        final int urgency = notification.getUrgency();
        final int lifespan = notification.getLifespan();
        final String summary = notification.getSummary();
        logger.debug("Dispatching notification: U[{}], L[{}], S[{}]", urgency, lifespan, summary);

        synchronized (channels) {
            channels.stream()
                    .filter(c -> urgency >= c.lowestUrgency() && urgency <= c.highestUrgency())
                    .filter(c -> lifespan >= c.lowestLifespan() && lifespan <= c.highestLifespan())
                    .forEach(c -> c.sendNotification(notification));
        }
    }
}
