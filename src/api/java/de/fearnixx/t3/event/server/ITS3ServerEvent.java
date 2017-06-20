package de.fearnixx.t3.event.server;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.ts3.ITS3Server;

/**
 * Created by MarkL4YG on 14.06.17.
 */
public interface ITS3ServerEvent extends IEvent {

    public ITS3Server getServer();

    public static interface IDataEvent extends ITS3ServerEvent {

        public static interface IClientsUpdated extends IDataEvent {}

        public static interface IChannelsUpdated extends IDataEvent {}
    }
}
