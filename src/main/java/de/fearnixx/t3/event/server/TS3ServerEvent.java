package de.fearnixx.t3.event.server;

import de.fearnixx.t3.ts3.ITS3Server;

/**
 * Created by MarkL4YG on 14.06.17.
 */
public class TS3ServerEvent implements ITS3ServerEvent {

    private ITS3Server svr;

    public TS3ServerEvent(ITS3Server server) {
        this.svr = server;
    }

    @Override
    public ITS3Server getServer() {
        return svr;
    }

    public static class DataEvent extends TS3ServerEvent implements IDataEvent {

        public DataEvent(ITS3Server server) {
            super(server);
        }

        public static class ClientsUpdated extends DataEvent implements IClientsUpdated {

            public ClientsUpdated(ITS3Server server) {
                super(server);
            }
        }

        public static class ChannelsUpdated extends DataEvent implements IChannelsUpdated {

            public ChannelsUpdated(ITS3Server server) {
                super(server);
            }
        }
    }
}
