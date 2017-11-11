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
public class QueryNotificationEvent extends QueryEvent.Message implements IQueryEvent.INotification {

    public QueryNotificationEvent(QueryConnection conn, IQueryMessage msg) {
        super(conn, null, msg);
    }

    public static class TargetClient extends QueryNotificationEvent implements IQueryEvent.INotification.ITargetClient {

        private TS3Client client;

        public TargetClient(QueryConnection conn, IQueryMessage msg) {
            super(conn, msg);
            this.client = new TS3Client();
            client.copyFrom(msg.getObjects().get(0));
        }

        @Override
        public IClient getTarget() {
            return client;
        }

        public static class ClientENTER extends TargetClient implements INotification.ITargetClient.IClientEnterView {
            public ClientENTER(QueryConnection conn, IQueryMessage msg) {
                super(conn, msg);
            }
        }

        public static class ClientLEAVE extends TargetClient implements INotification.ITargetClient.IClientLeftView {
            public ClientLEAVE(QueryConnection conn, IQueryMessage msg) {
                super(conn, msg);
            }
        }
    }

    public static class ClientMOVED extends QueryNotificationEvent implements INotification.IClientMoved {
        public ClientMOVED(QueryConnection conn, IQueryMessage msg) {
            super(conn, msg);
        }
    }

    public static class TextMessage extends QueryNotificationEvent implements IQueryEvent.INotification.ITextMessage {

        private IChatMessage chatMessage;

        public TextMessage(QueryConnection conn, QueryNotification.TextMessage textMessageNot) {
            super(conn, textMessageNot);
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
}
