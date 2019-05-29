package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Abstract representation of clients that are offline.
 * Base interface for all clients.
 */
public interface IUser {
    /**
     * @return The database ID of this client
     */
    Integer getClientDBID();

    /**
     * @return The unique client identifier
     */
    String getClientUniqueID();

    /**
     * @return The clients nick name
     */
    String getNickName();

    /**
     * @return The clients custom description.
     */
    String getDescription();

    /**
     * CRC32 checksum of the icon associated with the client.
     * Empty if none is set.
     *
     * @implNote The fix applied to {@link IChannel#}
     */
    String getIconID();

    /**
     * IDs for the clients server groups.
     */
    List<Integer> getGroupIDs();

    /**
     * The time, the clients uuid has been registered the first time.
     */
    Long getCreated();

    /**
     * Translation of {@link #getCreated()}.
     */
    LocalDateTime getCreatedTime();

    /**
     * The time, the clients uuid has joined the last time.
     */
    Long getLastJoin();

    /**
     * Translation of {@link #getLastJoin()}.
     */
    LocalDateTime getLastJoinTime();

    /**
     * Returns a {@link IQueryRequest} that can be used to add the client to server groups.
     */
    IQueryRequest addServerGroup(Integer... serverGroupIds);

    /**
     * Returns a {@link IQueryRequest} that can be used to remove the client from server groups.
     */
    IQueryRequest removeServerGroup(Integer... serverGroupIds);

    /**
     * Returns a {@link IQueryRequest} that can be used to set the clients channel group.
     *
     * @apiNote There is no method for "the current channel" as we want plugins to explicitly specify the channel.
     */
    IQueryRequest setChannelGroup(Integer channelId, Integer channelGroupId);
}
