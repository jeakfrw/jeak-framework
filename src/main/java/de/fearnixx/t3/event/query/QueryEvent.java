package de.fearnixx.t3.event.query;

import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.teamspeak.data.DataHolder;
import de.fearnixx.t3.teamspeak.data.IChannel;
import de.fearnixx.t3.teamspeak.data.IClient;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.QueryConnection;

import java.util.List;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public abstract class QueryEvent extends DataHolder implements IQueryEvent {

    private QueryConnection connection;

    @Override
    public IQueryConnection getConnection() {
        return connection;
    }

    public void setConnection(QueryConnection connection) {
        this.connection = connection;
    }

    public static abstract class DataEvent extends QueryEvent implements IDataEvent {

        public static class RefreshClients extends DataEvent implements IRefreshClients {
        }

        public static class RefreshChannels extends DataEvent implements IRefreshChannels {
        }
    }

    public static class Answer extends QueryEvent implements IQueryEvent.IAnswer {

        private IQueryRequest request;
        private List<IDataHolder> chain;

        @Override
        public IQueryRequest getRequest() {
            return request;
        }

        public void setRequest(IQueryRequest request) {
            this.request = request;
        }

        @Override
        public List<IDataHolder> getChain() {
            return chain;
        }

        public void setChain(List<IDataHolder> chain) {
            this.chain = chain;
        }
    }

    public static abstract class Notification extends QueryEvent implements IQueryEvent.INotification {

        private String caption;

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        /**
         * Tells the notification to read its DataHolder.
         */
        public void readFrom() {

        }
    }

    public static abstract class TargetClient extends Notification implements INotification.ITargetClient {

        private IClient client;

        public void setClient(IClient client) {
            this.client = client;
        }

        @Override
        public IClient getTarget() {
            return client;
        }
    }

    public static class ClientLeave extends TargetClient implements IQueryEvent.INotification.IClientLeave {
    }

    public static class ClientEnter extends TargetClient implements IQueryEvent.INotification.IClientEnter {
    }

    public static class ClientMoved extends TargetClient implements IQueryEvent.INotification.IClientMoved {
    }

    public static class ClientTextMessage extends TargetClient implements IQueryEvent.INotification.IClientTextMessage {
    }

    public static abstract class TargetChannel extends Notification implements INotification.ITargetChannel {

        private IChannel channel;

        public void setChannel(IChannel channel) {
            this.channel = channel;
        }

        @Override
        public IChannel getTarget() {
            return channel;
        }
    }

    public static class ChannelEdit extends TargetChannel {
    }

    public static class ChannelDelete extends TargetChannel {
    }

    public static class ChannelCreate extends TargetChannel {
    }

    public static class ChannelTextMessage extends TargetChannel implements IQueryEvent.INotification.IChannelTextMessage {
    }

    public static abstract class TargetServer extends Notification {
    }

    public static class ServerTextMessage extends TargetServer implements IQueryEvent.INotification.IServerTextMessage {
    }
}
