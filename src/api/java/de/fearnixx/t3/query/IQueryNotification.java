package de.fearnixx.t3.query;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public interface IQueryNotification {

    public enum NotifyType {
        UNKNOWN,
        VIEW_CLIENT_ENTER,
        VIEW_CLIENT_LEAVE
    }
    NotifyType getNotificationType();
}
