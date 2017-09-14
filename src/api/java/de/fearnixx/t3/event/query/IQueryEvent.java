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

    interface IMessage extends IQueryEvent {
        IQueryMessage getMessage();
        IQueryRequest getRequest();
    }

    interface INotification extends IQueryEvent {

        IQueryMessage getMessage();

        interface ITargetClient extends INotification {

            IClient getTarget();

            interface IClientEnterView extends ITargetClient {
            }

            interface IClientLeftView extends ITargetClient {
            }
        }

        interface ITextMessage extends INotification {

            ICommMessage getTextMessage();

            interface ITextPrivate extends ITextMessage {}

            interface ITextChannel extends ITextMessage {}

            interface ITextServer extends ITextMessage {}
        }
    }
}
