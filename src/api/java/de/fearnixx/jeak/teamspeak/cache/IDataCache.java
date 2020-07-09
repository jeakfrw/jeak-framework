package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * Access provider to the internal cache for online clients and channels.
 * Caches are refreshed using the {@code clientlist} and {@code channellist} commands regularly.
 * See {@link #getChannelRefreshTime()} and ¬{@link #getClientRefreshTime()} for the refresh intervals.
 * </p>
 * <p>
 * This interface and its methods should be used for working with channels and bulk-processing of clients who are currently online.
 * For getting specific clients or offline representations {@link IUserService} should be used.
 * </p>
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

    /**
     * Most recently cached version of {@link de.fearnixx.jeak.teamspeak.QueryCommands.SERVER#SERVER_INFO}.
     */
    Optional<IDataHolder> getServerInfo();

    /**
     * Most recently cached version of {@link de.fearnixx.jeak.teamspeak.QueryCommands.SERVER#INSTANCE_INFO}.
     */
    Optional<IDataHolder> getInstanceInfo();
}
