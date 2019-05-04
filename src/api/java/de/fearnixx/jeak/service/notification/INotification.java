package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.List;

/**
 * In order to unify one-way communication with clients, notifications can be invoked.
 * Notifications will be delivered over different registered {@link INotificationChannel}s.
 */
public interface INotification {

    static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    /**
     * List of unique IDs to which the notification should be sent.
     * Retrieve from: {@link IClient#getClientUniqueID()}
     */
    List<String> getRecipients();

    /**
     * The urgency level is used to determine the communication channels used to deliver the notification.
     * A higher urgency level means that the notification is right to be delivered via a more disruptive channel.
     * (For example, compare "sending a text message" with "sending a poke")
     */
    int getUrgency();

    /**
     * The lifespan level is used to determine the communication channels used to deliver the notification.
     * A lower lifespan means that the notification will not be delivered over "long-term" channels.
     * High-lifespan notifications will also not be sent using "short-term" channels.
     * (For example, compare "sending a text message" with "sending an E-Mail")
     */
    int getLifespan();

    /**
     * A very brief summary of what the notification is about.
     * (For example: The E-Mail channel will use this as the subject line)
     */
    String getSummary();

    /**
     * Short message of the notification.
     * Not strictly limited to a character count but channels may crop it!
     * (For example: Since TeamSpeak messages and pokes are length-restricted, this will be used for those channels.)
     * The TS limits are:
     * * TextMessage: 1024 characters
     * * PokeMessage: 100 characters
     *   (Due to this limit, the poke-channel may use a custom text when that length is exceeded)
     */
    String getShortText();

    /**
     * (Long) Message of the notification.
     * Channels which do not have a char count restriction (or a reasonably high derived one) will use this.
     */
    String getLongText();
}
