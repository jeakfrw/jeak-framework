package de.fearnixx.t3.event.query;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.query.IQueryConnection;
import de.fearnixx.t3.query.IQueryMessage;
import de.fearnixx.t3.query.IQueryRequest;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IQueryEvent extends IEvent {

    IQueryConnection getConnection();

    public static interface IMessage extends IQueryEvent {
        IQueryMessage getMessage();
        IQueryRequest getRequest();
    }

    public static interface INotifyEvent extends IQueryEvent {

        IQueryMessage getMessage();

        public static interface IClientEnterView extends INotifyEvent {}

        public static interface IClientLeftView extends INotifyEvent {}
    }
}
