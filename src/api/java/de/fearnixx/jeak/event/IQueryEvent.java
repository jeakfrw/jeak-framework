package de.fearnixx.jeak.event;

import de.fearnixx.jeak.teamspeak.NotificationReason;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * Base interface for all events fired by an {@link IQueryConnection} instance.
 */
public interface IQueryEvent extends IEvent {

    /**
     * The connection this event originated from.
     */
    IQueryConnection getConnection();

    /**
     * Event interface indicating that processed cached data has been updated.
     */
    interface IDataEvent extends IQueryEvent {

        /**
         * Event indicating that the client cache has been updated.
         * By default this happens around once every 60 seconds.
         */
        interface IRefreshClients extends IDataEvent {

            /**
             * The current state of the cache as a flat list.
             *
             * @implNote collection is unmodifiable!
             */
            List<IClient> getClients();


            /**
             * The current state of the cache as a map.
             * {@link IClient#getClientID()} -> {@link IClient}
             *
             * @implNote collection is unmodifiable!
             */
            Map<Integer, IClient> getClientMap();
        }

        /**
         * Event indicating that the channel cache has been updated.
         * By default this happens around once every 3 Minutes.
         */
        interface IRefreshChannels extends IDataEvent {

            /**
             * The current state of the cache as a flat list.
             *
             * @implNote collection is unmodifiable!
             */
            List<IChannel> getChannels();

            /**
             * The current state of the cache as a map.
             * {@link IChannel#getID()} -> {@link IChannel}
             *
             * @implNote collection is unmodifiable!
             */
            Map<Integer, IChannel> getChannelMap();
        }
    }

    /**
     * Event interface indicating that a request has received an answer.
     */
    interface IAnswer extends IQueryEvent {

        /**
         * The request that has requested/received the response.
         */
        IQueryRequest getRequest();

        /**
         * The KV chain that has been received.
         */
        List<IDataHolder> getDataChain();

        /**
         * The error response returned by TS3.
         */
        IRawQueryEvent.IMessage.IErrorMessage getError();

        /**
         * Shorthand to error code.
         */
        Integer getErrorCode();

        /**
         * Shorthand to error message.
         */
        String getErrorMessage();
    }

    /**
     * Event interface indicating that TS3 fired an event notification.
     */
    interface INotification extends IQueryEvent, IDataHolder {

        /**
         * Caption of the event provided by TeamSpeak.
         * The "notify" prefix is stripped from the caption therefore this is really just the identifier.
         */
        String getCaption();

        interface IClientEnter extends ITargetClient, IQueryEvent.INotification {

            /**
             * Numerical reason id provided by TS3.
             * It is recommended
             */
            Integer getReasonId();

            /**
             * Typed representation of {@link #getReasonId()}.
             */
            NotificationReason getReason();

            /**
             * When a client comes back from being invisible to the query client,
             * this will be set to the origin channel id.
             * {@code 0} will be used if the client just connected to the server.
             */
            Integer getOriginChannelId();

            /**
             * The target channel the client moved/connected to.
             */
            Integer getTargetChannelId();
        }

        interface IClientLeave extends ITargetClient, IQueryEvent.INotification {

            /**
             * Numerical reason id provided by TS3.
             * It is recommended to use {@link #getReason()} instead.
             */
            Integer getReasonId();

            /**
             * Typed representation of {@link #getReasonId()}.
             */
            NotificationReason getReason();

            Integer getOriginChannelId();

            Integer getTargetChannelId();
        }

        interface IClientMoved extends ITargetClient, IQueryEvent.INotification {

            Integer getTargetChannelId();

            Integer getReasonId();

            Boolean wasSelf();

            Boolean wasForced();

            Boolean wasServer();
        }

        interface ITextMessage extends INotification {

            /**
             * The message that has been sent.
             */
            String getMessage();

            /**
             * The {@link IClient#getClientID()} of the sender.
             */
            Integer getInvokerId();

            /**
             * The {@link IClient#getClientUniqueID()} of the sender.
             */
            String getInvokerUID();

            /**
             * The {@link IClient#getNickName()} of the sender.
             */
            String getInvokerName();

            /**
             * Numerical representation of the target mode (client, channel, server)
             * It is recommended to use {@link #getTargetMode()} instead.
             */
            Integer getTargetModeId();

            /**
             * The typed representation of {@link #getTargetModeId()}.
             * Whether or not the text message was private (client), channel or server scoped.
             */
            TargetType getTargetMode();
        }

        interface IClientTextMessage extends ITextMessage, ITargetClient {
        }

        interface IChannelTextMessage extends ITextMessage, ITargetChannel {
        }

        interface IServerTextMessage extends ITextMessage, ITargetServer {
        }

        interface IChannelCreated extends ITargetChannel {
        }

        interface IChannelDeleted extends ITargetChannel {
        }

        interface IChannelMoved extends IChannelEdited {
        }

        interface IChannelEdited extends ITargetChannel, IQueryEvent.INotification {

            /**
             * Channel properties and their new values.
             *
             * @implNote changes for {@link IChannelEditedDescription} and
             * {@link IChannelPasswordChanged} are always empty!
             */
            Map<String, String> getChanges();

        }

        interface IChannelEditedDescription extends IChannelEdited {
        }

        interface IChannelPasswordChanged extends IChannelEdited {
        }
    }

    /**
     * Returns the first reference of the raw event.
     *
     * @deprecated To discourage use of this! If you use this you better have a very good reason to do so!
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    IRawQueryEvent getRawReference();
}
