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

    /**
     * Created by MarkL4YG on 31.05.17.
     */
    interface INotification extends IMessage {

        IQueryMessage.MsgType getType();

        public interface Server extends INotification {

        }

        public interface Channel extends INotification {

        }

        public interface TextServer extends INotification {

        }

        public interface TextChannel extends INotification {

        }

        public interface TextPrivate extends INotification {

        }
    }
}
