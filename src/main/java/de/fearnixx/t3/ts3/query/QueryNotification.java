package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.query.IQueryNotification;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public class QueryNotification implements IQueryNotification {

    private NotifyType type;

    public QueryNotification(NotifyType type) {
        this.type = type;
    }
    public NotifyType getNotificationType() {
        return type;
    }
}
