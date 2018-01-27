package de.fearnixx.t3.event.query;

import de.fearnixx.t3.ts3.chat.IChatMessage;
import de.fearnixx.t3.ts3.query.IQueryMessage;
import de.fearnixx.t3.ts3.client.IClient;
import de.fearnixx.t3.ts3.client.TS3Client;
import de.fearnixx.t3.ts3.query.QueryConnection;
import de.fearnixx.t3.ts3.query.QueryNotification;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public class QueryNotificationEvent extends QueryEvent implements IQueryEvent.INotification {

    public QueryNotificationEvent(QueryConnection conn) {
        super(conn);
    }

    public static class ClientENTER extends QueryNotificationEvent implements INotification.IClientEnterView {
        public ClientENTER(QueryConnection conn) {
            super(conn);
        }
    }

    public static class ClientLEAVE extends QueryNotificationEvent implements INotification.IClientLeftView {
        public ClientLEAVE(QueryConnection conn) {
                super(conn);
            }
    }

    public static class ClientMOVED extends QueryNotificationEvent implements INotification.IClientMoved {
        public ClientMOVED(QueryConnection conn) {
            super(conn);
        }
    }

    public static class TextMessage extends QueryNotificationEvent implements IQueryEvent.INotification.ITextMessage {

        private IChatMessage chatMessage;

        public TextMessage(QueryConnection conn, QueryNotification.TextMessage textMessageNot) {
            super(conn);
            this.setMessage(textMessageNot);
            this.chatMessage = textMessageNot.getChatMessage();
        }

        @Override
        public IChatMessage getChatMessage() {
            return chatMessage;
        }

        public static class TextPrivate extends TextMessage implements IQueryEvent.INotification.ITextMessage.ITextPrivate {

            public TextPrivate(QueryConnection conn, QueryNotification.TextMessage textMessageNot) {
                super(conn, textMessageNot);
            }
        }

        public static class TextChannel extends TextMessage implements IQueryEvent.INotification.ITextMessage.ITextChannel {

            public TextChannel(QueryConnection conn, QueryNotification.TextMessage textMessageNot) {
                super(conn, textMessageNot);
            }
        }

        public static class TextServer extends TextMessage implements IQueryEvent.INotification.ITextMessage.ITextServer {

            public TextServer(QueryConnection conn, QueryNotification.TextMessage textMessageNot) {
                super(conn, textMessageNot);
            }
        }
    }

    public static class ChannelCreated extends QueryNotificationEvent implements IQueryEvent.INotification.IChannelCreated {

        public ChannelCreated(QueryConnection conn) {
            super(conn);
        }
    }
}
