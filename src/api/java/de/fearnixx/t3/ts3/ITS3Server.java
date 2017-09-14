package de.fearnixx.t3.ts3;

import de.fearnixx.t3.ts3.query.IQueryConnection;
import de.fearnixx.t3.ts3.channel.IChannel;
import de.fearnixx.t3.ts3.client.IClient;

import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 11.06.17.
 *
 * Abstract representation of a TS3 server instance
 * See each method for additional information
 */
public interface ITS3Server {

    /**
     * The abstract connection to the server
     * @return The main connection
     */
    IQueryConnection getConnection();

    /**
     * List of all clients
     * @implNote Refreshed periodically thus always slightly outdated!
     * @return List of all clients
     */
    List<IClient> getClientList();

    /**
     * Map of all clients by ClientID
     * @see #getClientList()  for additional info
     * @return Map of all clients
     */
    Map<Integer, IClient> getClientMap();


    /**
     * List of all channels
     * @implNote Refreshed periodically thus always slightly outdated!
     * @return List of all channels
     */
    List<IChannel> getChannelList();

    /**
     * Map of all channels by channel ID
     * @see #getChannelList() for additional info
     * @return Map of all channels
     */
    Map<Integer, IChannel> getChannelMap();
}
