package de.fearnixx.jeak.event.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public abstract class QueryEvent extends BasicDataHolder implements IQueryEvent {

    private IQueryConnection connection;
    private IRawQueryEvent rawReference;

    @Override
    public IQueryConnection getConnection() {
        return connection;
    }

    public void setConnection(IQueryConnection connection) {
        this.connection = connection;
    }

    @Override
    public IRawQueryEvent getRawReference() {
        return rawReference;
    }

    public void setRawReference(IRawQueryEvent rawReference) {
        this.rawReference = rawReference;
    }

    public abstract static class BasicDataEvent extends QueryEvent implements IDataEvent {

        public static class RefreshClients extends BasicDataEvent implements IRefreshClients {
        }

        public static class RefreshChannels extends BasicDataEvent implements IRefreshChannels {
        }
    }

    public static class Answer extends QueryEvent implements IQueryEvent.IAnswer {

        private IQueryRequest request;
        private List<IDataHolder> chain;
        private IRawQueryEvent.IMessage.IErrorMessage error;

        @Override
        public IQueryRequest getRequest() {
            return request;
        }

        public void setRequest(IQueryRequest request) {
            this.request = request;
        }

        @Override
        public List<IDataHolder> getChain() {
            return getDataChain();
        }

        @Override
        public List<IDataHolder> getDataChain() {
            return chain;
        }

        public void setChain(List<IDataHolder> chain) {
            this.chain = chain;
        }

        public void setError(IRawQueryEvent.IMessage.IErrorMessage error) {
            this.error = error;
        }

        @Override
        public IRawQueryEvent.IMessage.IErrorMessage getError() {
            return error;
        }
    }

    public abstract static class Notification extends QueryEvent implements IQueryEvent.INotification {

        private String caption;

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public abstract static class TargetClient extends Notification implements INotification.ITargetClient {

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

    public abstract static class TargetChannel extends Notification implements INotification.ITargetChannel {

        private IChannel channel;

        public void setChannel(IChannel channel) {
            this.channel = channel;
        }

        @Override
        public IChannel getTarget() {
            return channel;
        }
    }

    public static class ChannelEdit extends TargetChannel implements INotification.IChannelEdited {

        private final Map<String, String> deltas;

        public ChannelEdit(Map<String, String> deltas) {
            this.deltas = Collections.unmodifiableMap(deltas);
        }

        protected ChannelEdit() {
            this.deltas = Collections.emptyMap();
        }

        @Override
        public Map<String, String> getChanges() {
            return deltas;
        }
    }

    public static class ChannelEditDescr extends ChannelEdit implements INotification.IChannelEditedDescription {
    }

    public static class ChannelPasswordChanged extends ChannelEdit implements INotification.IChannelPasswordChanged {
    }

    public static class ChannelDelete extends TargetChannel implements INotification.IChannelDeleted {
    }

    public static class ChannelCreate extends TargetChannel implements INotification.IChannelCreated {
    }

    public static class ChannelTextMessage extends TargetChannel implements IQueryEvent.INotification.IChannelTextMessage {
    }

    public abstract static class TargetServer extends Notification {
    }

    public static class ServerTextMessage extends TargetServer implements IQueryEvent.INotification.IServerTextMessage {
    }

    public static class ChannelMoved extends ChannelEdit implements INotification.IChannelMoved {
    }
}
