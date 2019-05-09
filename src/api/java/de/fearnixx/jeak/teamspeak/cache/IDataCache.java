package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Searches for a client with that unique ID. Returns the first match as an optional.
     */
    Optional<IClient> findClientByUniqueId(String uniqueId);

    /**
     * Searches for a channel with that unique ID. Returns the first match as an optional.
     * Name will match case-insensitive and partially.
     */
    Optional<IChannel> findChannelByName(String name);

    /**
     * Returns the configured refresh period for clients in seconds.
     */
    int getClientRefreshTime();

    /**
     * Returns the configured refresh period for channels in seconds.
     */
    int getChannelRefreshTime();
}
