package de.fearnixx.jeak.event.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.event.ITargetChannel;
import de.fearnixx.jeak.event.ITargetClient;
import de.fearnixx.jeak.teamspeak.NotificationReason;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
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

            private List<IClient> clients;
            private Map<Integer, IClient> clientMap;

            public RefreshClients(List<IClient> clients, Map<Integer, IClient> clientMap) {
                this.clients = clients;
                this.clientMap = clientMap;
            }

            public List<IClient> getClients() {
                return clients;
            }

            public Map<Integer, IClient> getClientMap() {
                return clientMap;
            }
        }

        public static class RefreshChannels extends BasicDataEvent implements IRefreshChannels {

            private List<IChannel> channels;
            private Map<Integer, IChannel> channelMap;

            public RefreshChannels(List<IChannel> channels, Map<Integer, IChannel> channelMap) {
                this.channels = channels;
                this.channelMap = channelMap;
            }

            @Override
            public List<IChannel> getChannels() {
                return channels;
            }

            @Override
            public Map<Integer, IChannel> getChannelMap() {
                return channelMap;
            }
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

        @Override
        public Integer getErrorCode() {
            return getError().getCode();
        }

        @Override
        public String getErrorMessage() {
            return getError().getMessage();
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

    public abstract static class TargetClient extends Notification implements ITargetClient {

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

        @Override
        public Integer getReasonId() {
            return getProperty("reasonid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientLeave-Event without reasonid!"));
        }

        @Override
        public NotificationReason getReason() {
            return NotificationReason.forReasonId(getReasonId());
        }

        @Override
        public Integer getOriginChannelId() {
            return getProperty("cfid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientLeave-Event without original channel id!"));
        }

        @Override
        public Integer getTargetChannelId() {
            return getProperty("ctid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientLeave-Event without target channel id!"));
        }
    }

    public static class ClientEnter extends TargetClient implements IQueryEvent.INotification.IClientEnter {

        @Override
        public Integer getReasonId() {
            return getProperty("reasonid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientEnter-Event without reasonid!"));
        }

        @Override
        public NotificationReason getReason() {
            return NotificationReason.forReasonId(getReasonId());
        }

        @Override
        public Integer getOriginChannelId() {
            return getProperty("cfid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientEnter-Event without original channel id!"));
        }

        @Override
        public Integer getTargetChannelId() {
            return getProperty("ctid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientEnter-Event without target channel id!"));
        }
    }

    public static class ClientMoved extends TargetClient implements IQueryEvent.INotification.IClientMoved {

        @Override
        public Integer getTargetChannelId() {
            return this.getProperty("ctid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientMoved-Event has no ctid!"));
        }

        @Override
        public Integer getReasonId() {
            return this.getProperty("reasonid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("ClientMoved-Event has no reasonid!"));
        }
    }

    public static class ClientTextMessage extends TargetClient implements TextMessageEvent, INotification.IClientTextMessage {
    }

    public abstract static class TargetChannel extends Notification implements ITargetChannel {

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

    public interface TextMessageEvent extends INotification.ITextMessage {

        @Override
        default String getMessage() {
            return getProperty(PropertyKeys.TextMessage.MESSAGE)
                    .orElseThrow(() -> new ConsistencyViolationException("Text message without message!"));
        }

        @Override
        default Integer getInvokerId() {
            return this.getProperty("invokerid")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("TextMessage-Event has no invokerid!"));
        }

        @Override
        default String getInvokerUID() {
            return this.getProperty("invokeruid")
                    .orElseThrow(() -> new ConsistencyViolationException("TextMessage-Event has no invokerUID!"));
        }

        @Override
        default String getInvokerName() {
            return this.getProperty("invokername")
                    .orElseThrow(() -> new ConsistencyViolationException("TextMessage-Event has no invokername!"));
        }

        @Override
        default Integer getTargetModeId() {
            return this.getProperty("targetmode")
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new ConsistencyViolationException("TextMessage-Event has no targetmode!"));
        }

        @Override
        default TargetType getTargetMode() {
            return TargetType.fromQueryNum(getTargetModeId());
        }
    }

    public static class ChannelTextMessage extends TargetChannel implements TextMessageEvent, INotification.IChannelTextMessage {
    }

    public abstract static class TargetServer extends Notification {
    }

    public static class ServerTextMessage extends TargetServer implements TextMessageEvent, INotification.IServerTextMessage {
    }

    public static class ChannelMoved extends ChannelEdit implements INotification.IChannelMoved {
    }
}
