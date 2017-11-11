package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.ts3.keys.NotificationType;

import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public interface IQueryConnection {

    /**
     * Issues a query login.
     *
     * Sends the commands "login" and "use"
     *
     * Blocks the thread until the answers are available
     * @param instanceID The desired virtualserver instance ID (0 for none)
     * @param user The user to login with
     * @param pass The password to login with
     * @return Whether or not all commands succeeded
     */
    boolean blockingLogin(Integer instanceID, String user, String pass);

    /**
     * @return The currently selected server instance ID
     */
    Integer getInstanceID();

    /**
     * Sets the nickname of this server query connection
     * @param newNickName The new nickname
     */
    void setNickName(String newNickName);

    /**
     * @return The last response to the "whoami" command - contains sometimes useful information
     */
    IQueryMessage getWhoAmI();

    /**
     * Send a request.
     *
     * This queues the request and sends it when possible.
     * @param req The request to send
     */
    void sendRequest(IQueryRequest req);

    /**
     * Send a request
     *
     * This queues the request and sends it when possible.
     * The callback will be called when a full answer has been received
     * @param req The request
     * @param onDone The callback consumer
     */
    void sendRequest(IQueryRequest req, Consumer<IQueryEvent.IMessage> onDone);

    /**
     * @see #subscribeNotification(NotificationType, Integer)
     */
    void subscribeNotification(NotificationType type);

    /**
     * Subscribes to a notification event
     * @param type The type to subscribe to
     * @param channelID Can be null for non-channel-specific events
     */
    void subscribeNotification(NotificationType type, Integer channelID);

    /**
     * @see #unsubscribeNotification(NotificationType, Integer)
     */
    void unsubscribeNotification(NotificationType type);

    /**
     * Undos {@link #subscribeNotification(NotificationType, Integer)}
     * @param type The type to unsubscribe from
     * @param channelID Can be null for non-channel-specific events
     */
    void unsubscribeNotification(NotificationType type, Integer channelID);
}
