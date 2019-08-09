package de.fearnixx.jeak.service.notification;

/**
 * A notification channel is used to deliver messages.
 */
public interface INotificationChannel {

    int lowestUrgency();

    int highestUrgency();

    int lowestLifespan();

    int highestLifespan();

    void sendNotification(INotification notification);
}
