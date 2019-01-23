package de.fearnixx.jeak.service.notification;

/**
 * Service interface for dispatching notifications to a registered pool of channels.
 */
public interface INotificationService {

    /**
     * Dispatches a notification to all channels which match the contextual information of the notification.
     * @see INotification for more information.
     */
    void dispatch(INotification notification);

    /**
     * Plugins can provide custom notification channels for the notification service.
     * Based on the settings of the channel, the channel will receive notifications to send.
     * @see INotificationChannel for more information.
     */
    void registerChannel(INotificationChannel channel);
}
