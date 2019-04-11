package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 20.06.17.
 * <p>
 * Abstract representation of online clients
 */
public interface IClient extends IDataHolder {

    Boolean isValid();

    /**
     * @return The clients ID
     * @apiNote This is only valid while the client is online. Refer to {@link #getClientDBID()}
     */
    Integer getClientID();

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
     * CRC32 checksum of the icon associated with the client.
     * Empty if none is set.
     *
     * @implNote The fix applied to {@link IChannel#}
     */
    String getIconID();

    enum PlatformType {
        UNKNOWN,
        WINDOWS,
        LINUX,
        ANDROID,
        OSX,
        IOS
    }

    PlatformType getPlatform();

    /**
     * TS3 client version of that client.
     */
    String getVersion();

    enum ClientType {
        VOICE,
        QUERY
    }

    ClientType getClientType();

    /**
     * ID of the channel, the client is currently joined.
     *
     * @implNote This is updated dynamically outside of the usual cache-refresh after a move-event.
     */
    Integer getChannelID();

    /**
     * ID of the channel group that is currently associated with the client.
     *
     * @implNote Contary to the channel id, this is NOT dynamically updated at the moment.
     */
    Integer getChannelGroupID();

    /**
     * The ID of the channel where the client receives the associated channel group from.
     * This is not the same as {@link #getChannelID()} for inherited channel groups.
     */
    Integer getChannelGroupSource();

    /**
     * Whether or not the client has currently set an AFK status.
     */
    Boolean isAway();

    /**
     * The message used by the client for the AFK status.
     */
    String getAwayMessage();

    /**
     * The current talk power value.
     * Must be {@code >= {@link IChannel#getTalkPower()}} for the client to be allowed to talk.
     */
    Integer getTalkPower();

    /**
     * Whether or not the client was seen talking during the last cache refresh.
     *
     * @apiNote Not that useful, but available.
     */
    Boolean isTalking();

    /**
     * Whether or not the client is currently able to talk.
     */
    Boolean isTalker();

    /**
     * Priority talkers cause clients to lower non-priority talkers volume when they speak.
     * This allows moderators to be heard even when some or many others are talking.
     * This can be forced-on by TS3 permissions.
     */
    Boolean isPrioTalker();

    /**
     * Same effect as {@link #isPrioTalker()} but with different configuration values and on a channel-level.
     */
    Boolean isCommander();

    /**
     * Whether or not the client is currently recording (via. TS3s built-in recorder).
     */
    Boolean isRecording();

    /**
     * Whether or not the microphone of the client is currently not indicated as deactivated.
     */
    Boolean hasMic();

    /**
     * Whether or not the client has currently manually muted the microphone.
     */
    Boolean hasMicMuted();

    /**
     * Whether or not the speakers of the client are currently not indicated as deactivated.
     */
    Boolean hasOutput();

    /**
     * Whether or not the client has currently manually muted the speakers.
     */
    Boolean hasOutputMuted();

    /**
     * IDs for the clients server groups.
     */
    List<Integer> getGroupIDs();

    /**
     * For how long the client has been idle.
     *
     * @implNote the value is provided by TeamSpeak.
     */
    Integer getIdleTime();

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
     * Returns a {@link IQueryRequest} that can be used to send a message to this client.
     */
    IQueryRequest sendMessage(String message);

    /**
     * Returns a {@link IQueryRequest} that can be used to send a poke to this client.
     */
    IQueryRequest sendPoke(String message);

    /**
     * Returns a {@link IQueryRequest} that can be used to edit the given channel properties.
     * Use {@link PropertyKeys.Client} for the property names.
     */
    IQueryRequest edit(Map<String, String> properties);

    /**
     * Returns a {@link IQueryRequest} that can be used to edit the clients description.
     */
    IQueryRequest setDescription(String clientDescription);

    /**
     * Returns a {@link IQueryRequest} that can be used to move the client to another channel.
     */
    IQueryRequest moveToChannel(Integer channelId);

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

    /**
     * Returns a {@link IQueryRequest} that can be used to kick the client form the server.
     */
    IQueryRequest kickFromServer(String reasonMessage);

    /**
     * @see #kickFromServer(String)
     */
    IQueryRequest kickFromServer();

    /**
     * Returns a {@link IQueryRequest} that can be used to kick the client from the channel.
     */
    IQueryRequest kickFromChannel(String reasonMessage);

    /**
     * @see #kickFromChannel(String)
     */
    IQueryRequest kickFromChannel();

    /**
     * Returns a {@link IQueryRequest} that can be used to ban the client from the server.
     */
    IQueryRequest ban(String reasonMessage, TimeUnit durationUnit, Integer duration);

    /**
     * @see #ban(String, TimeUnit, Integer)
     */
    IQueryRequest ban(String reasonMessage, Integer durationInSeconds);

    /**
     * @see #ban(String, TimeUnit, Integer)
     */
    IQueryRequest banPermanent(String reasonMessage);
}
