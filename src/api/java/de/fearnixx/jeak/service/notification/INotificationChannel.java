package de.fearnixx.jeak.service.notification;

public interface INotificationChannel {

    int lowestUrgency();

    int highestUrgency();

    int lowestLifespan();

    int highestLifespan();

    void sendNotification(INotification notification);
}
