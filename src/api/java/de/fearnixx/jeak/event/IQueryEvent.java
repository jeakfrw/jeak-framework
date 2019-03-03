package de.fearnixx.jeak.event;

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
        }

        /**
         * Event indicating that the channel cache has been updated.
         * By default this happens around once every 3 Minutes.
         */
        interface IRefreshChannels extends IDataEvent {
        }
    }

    /**
     * Event interface indicating that a request has received an answer.
     */
    interface IAnswer extends IQueryEvent {

        /**
         * The request that has requested/received the response
         */
        IQueryRequest getRequest();

        /**
         * The KV chain that has been received.
         * @deprecated to comply with the deprecation of {@link IQueryRequest#getChain()}
         */
        @Deprecated
        List<IDataHolder> getChain();

        /**
         * The KV chain that has been received.
         */
        List<IDataHolder> getDataChain();

        /**
         * The error response returned by TS3.
         */
        IRawQueryEvent.IMessage.IErrorMessage getError();
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

        /**
         * Event that targeted a specific client.
         * The target is injected using a SystemListener.
         * In case the injection fails this event will not be fired to preserve consistency.
         */
        interface ITargetClient extends INotification {

            IClient getTarget();
        }

        /**
         * Event that targeted a specific channel.
         * The target is injected using a SystemListener.
         * In case the injection fails this event will not be fired to preserve consistency.
         */
        interface ITargetChannel extends INotification {

            IChannel getTarget();
        }

        interface ITargetServer extends INotification {
        }

        interface IClientEnter extends ITargetClient {
        }

        interface IClientLeave extends ITargetClient {
        }

        interface IClientMoved extends ITargetClient {
        }

        interface ITextMessage extends INotification {
        }

        interface IClientTextMessage extends ITextMessage,ITargetClient {
        }

        interface IChannelTextMessage extends ITextMessage,ITargetChannel{
        }

        interface IServerTextMessage extends ITextMessage,ITargetServer {
        }

        interface IChannelCreated extends ITargetChannel {
        }

        interface IChannelDeleted extends ITargetChannel {
        }

        interface IChannelMoved extends IChannelEdited {
        }

        interface IChannelEdited extends ITargetChannel {

            /**
             * Channel properties and their new values.
             * @implNote changes for {@link IChannelEditedDescription} and {@link IChannelPasswordChanged} are always empty!
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
