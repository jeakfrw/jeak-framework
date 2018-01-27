package de.fearnixx.t3.event.query;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.ts3.chat.IChatMessage;
import de.fearnixx.t3.ts3.query.IQueryConnection;
import de.fearnixx.t3.ts3.query.IQueryMessage;
import de.fearnixx.t3.ts3.query.IQueryRequest;
import de.fearnixx.t3.ts3.client.IClient;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IQueryEvent extends IEvent, IQueryMessage {

    IQueryConnection getConnection();

    interface IAnswer extends IQueryEvent {
        IQueryRequest getRequest();
    }

    interface INotification extends IQueryEvent {

        interface IClientEnterView extends INotification {
        }

        interface IClientLeftView extends INotification {
        }

        interface IClientMoved extends INotification {
        }

        interface ITextMessage extends INotification {

            IChatMessage getChatMessage();

            interface ITextPrivate extends ITextMessage {}

            interface ITextChannel extends ITextMessage {}

            interface ITextServer extends ITextMessage {}
        }

        interface IChannelCreated extends INotification {}

        interface IChannelDeleted extends INotification {}
    }
}
