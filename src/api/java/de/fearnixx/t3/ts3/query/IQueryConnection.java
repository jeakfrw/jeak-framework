package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.ts3.comm.ICommManager;
import de.fearnixx.t3.ts3.keys.NotificationType;

import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public interface IQueryConnection {

    boolean blockingLogin(Integer instanceID, String user, String pass);
    Integer getInstanceID();
    void setNickName(String newNickName);
    IQueryMessage getWhoAmI();

    ICommManager getCommManager();

    void sendRequest(IQueryRequest req);
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
