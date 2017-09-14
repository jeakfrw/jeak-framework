package de.fearnixx.t3.event.server;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.ts3.ITS3Server;

/**
 * Created by MarkL4YG on 14.06.17.
 */
public interface ITS3ServerEvent extends IEvent {

    ITS3Server getServer();

    interface IDataEvent extends ITS3ServerEvent {

        interface IClientsUpdated extends IDataEvent {}

        interface IChannelsUpdated extends IDataEvent {}
    }
}
