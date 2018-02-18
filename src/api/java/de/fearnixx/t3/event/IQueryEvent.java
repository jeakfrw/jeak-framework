package de.fearnixx.t3.event;

import de.fearnixx.t3.teamspeak.data.IChannel;
import de.fearnixx.t3.teamspeak.data.IClient;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;

import java.util.List;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public interface IQueryEvent extends IEvent {

    IQueryConnection getConnection();

    interface IDataEvent extends IQueryEvent {

        interface IRefreshClients extends IDataEvent {
        }

        interface IRefreshChannels extends IDataEvent {
        }
    }

    interface IAnswer extends IQueryEvent {

        IQueryRequest getRequest();

        List<IDataHolder> getChain();
    }

    interface INotification extends IQueryEvent, IDataHolder{

        interface ITargetClient extends INotification {

            IClient getTarget();
        }

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

        interface IChannelEdited extends ITargetChannel {
        }
    }
}
