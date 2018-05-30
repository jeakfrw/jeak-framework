package de.fearnixx.t3.teamspeak.cache;

import de.fearnixx.t3.teamspeak.data.IChannel;
import de.fearnixx.t3.teamspeak.data.IClient;

import java.util.List;
import java.util.Map;

/**
 * Cache accessor.
 */
public interface IDataCache {

    /**
     * Unmodifiable cache for clients by clientID.
     */
    Map<Integer, IClient> getClientMap();

    /**
     * Unmodifiable cache for channels by channelID.
     */
    Map<Integer, IChannel> getChannelMap();

    /**
     * Value accessor for {@link #getClientMap()} for convenience.
     */
    List<IClient> getClients();

    /**
     * Value accessor for {@link #getChannelMap()} for convenience.
     */
    List<IChannel> getChannels();
}
