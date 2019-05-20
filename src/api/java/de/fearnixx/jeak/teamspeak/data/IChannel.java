package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 15.06.17.
 *
 * Abstract representation of a channel using the information queried via the ServerQuery
 * Spacers extend this:
 * @see ISpacer
 *
 * @apiNote All values can be changed through the {@link IDataHolder} interface - At least on instances created by the bot itself
 * @apiNote Remember that changing the values will not actually edit the channel on the server!
 *
 * @implNote Channels acquired using {@link IDataCache#getChannels()} are currently updated regularly and thread safe.
 * @implNote That means obsolete channels are invalidated automatically through {@link #getPersistence()} as {@link ChannelPersistence#DELETED}
 * @implNote However the API can not make this guarantee for any custom implementations!
 */
public interface IChannel extends IDataHolder {

    /**
     * @return The channel ID
     */
    Integer getID();

    /**
     * @return The ID of this channels parent - 0 if none
     */
    Integer getParent();

    /**
     * @return The channels position under its parent
     */
    Integer getOrder();

    /**
     * @return The current channel name
     */
    String getName();

    /**
     * @return The current channel topic - "" if none
     */
    String getTopic();

    /**
     * @return If this is the default channel
     */
    Boolean isDefault();

    /**
     * @return If this channel is password protected
     */
    Boolean hasPassword();


    /**
     * Based on the channel name. The following prefixes are considered a spacer
     * - "[spacer<int/>]"
     * - "[cspacer<int/>]"
     * - "[*spacer<int/>]"
     * - "[*cspacer<int/>]"
     * - All above using a float instead
     * @return If this channel is a spacer
     */
    Boolean isSpacer();

    /**
     * @implNote This becomes {@link ChannelPersistence#DELETED} once the server received a channellist response without the channel ID. Any plugin should drop the object at that time
     * @return The channel persistence mode
     */
    ChannelPersistence getPersistence();
    enum ChannelPersistence {
        TEMPORARY,
        SEMI_PERMANENT,
        PERMANENT,
        DELETED
    }

    /**
     * @return The talk power required to talk
     */
    Integer getTalkPower();

    /**
     * @return The amount of clients in this channel
     */
    Integer getClientCount();

    /**
     * @implNote The maximum can be below the actual count! TS3 allows admins to ignore this limit
     * @return The maximum number of clients in this channel
     */
    Integer getMaxClientCount();

    /**
     * @return The total amount of clients below this channel
     */
    Integer getClientCountBelow();

    /**
     * @implNote The maximum can be below the actual count! TS3 allows admins to ignore this limit
     * @return The maximum number of clients below this channel
     */
    Integer getMaxClientCountBelow();

    /**
     * @return Integer representation of the channel codec
     */
    Integer getCodec();

    /**
     * @return The currently used codec quality
     */
    Integer getCodecQuality();

    /**
     * @return All sub channels of this channel
     */
    List<IChannel> getSubChannels();

    /**
     * Sends a message to this channel.
     * @deprecated <p>Not available at the moment. TS3 forces any messages directed at a channel to be sent to the current one.
     * As this is likely to be the default channel or a dedicated bot-channel, this is not useful.
     * A work-around is scheduled to be put in place: https://gitlab.com/fearnixxgaming/jeakbot/jeakbot-framework/issues/32
     * </p>
     */
    @Deprecated
    IQueryRequest sendMessage(String message);

    /**
     * @see #delete(boolean) with forced = {@code false}
     */
    IQueryRequest delete();

    /**
     * Returns a {@link IQueryRequest} that can be used to delete this channel.
     * The channel may not be deleted when forced is {@code false}.
     */
    IQueryRequest delete(boolean forced);

    /**
     * Returns a {@link IQueryRequest} that can be used to rename this channel.
     */
    IQueryRequest rename(String channelName);

    /**
     * Returns a {@link IQueryRequest} that can be used to move the channel below another one.
     * (Edits "channel_order_id", for "below" as in a tree structure, see {@link #moveInto}
     */
    IQueryRequest moveBelow(Integer channelAboveId);

    /**
     * Returns a {@link IQueryRequest} that can be used to move the channel into another one.
     * (Edits the parent id.)
     */
    IQueryRequest moveInto(Integer channelParentId);

    /**
     * Returns a {@link IQueryRequest} that can be used to edit the given channel properties.
     * Use {@link PropertyKeys.Channel} for the property names.
     */
    IQueryRequest edit(Map<String, String> properties);
}
