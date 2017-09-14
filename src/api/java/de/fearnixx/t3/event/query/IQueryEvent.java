package de.fearnixx.t3.event.query;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.ts3.query.IQueryConnection;
import de.fearnixx.t3.ts3.query.IQueryMessage;
import de.fearnixx.t3.ts3.query.IQueryRequest;
import de.fearnixx.t3.ts3.client.IClient;
import de.fearnixx.t3.ts3.comm.ICommMessage;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IQueryEvent extends IEvent {

    IQueryConnection getConnection();

    public static interface IMessage extends IQueryEvent {
        IQueryMessage getMessage();
        IQueryRequest getRequest();
    }

    public static interface INotification extends IQueryEvent {

        IQueryMessage getMessage();

        public static interface ITargetClient extends INotification {

            IClient getTarget();

            public static interface IClientEnterView extends ITargetClient {
            }

            public static interface IClientLeftView extends ITargetClient {
            }
        }

        public static interface ITextMessage extends INotification {

            ICommMessage getTextMessage();

            public static interface ITextPrivate extends ITextMessage {}

            public static interface ITextChannel extends ITextMessage {}

            public static interface ITextServer extends ITextMessage {}
        }
    }
}
