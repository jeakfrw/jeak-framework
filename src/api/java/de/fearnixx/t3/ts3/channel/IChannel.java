package de.fearnixx.t3.ts3.channel;

import de.fearnixx.t3.ts3.ITS3Server;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;

import java.util.List;

/**
 * Created by MarkL4YG on 15.06.17.
 *
 * Abstract representation of a channel using the information queried via the ServerQuery
 * Spacers extend this:
 * @see ISpacer
 *
 * @apiNote All values can be changed through the {@link IQueryMessageObject} interface - At least on instances created by the bot itself
 * @apiNote Remember that changing the values will not actually edit the channel on the server!
 *
 * @implNote Channels acquired using {@link ITS3Server#getChannelList()} are currently updated regularly and thread safe.
 * @implNote That means obsolete channels are invalidated automatically through {@link #getPersistence()} as {@link ChannelPersistence#DELETED}
 * @implNote However the API can not make this guarantee for any custom implementations!
 */
public interface IChannel extends IQueryMessageObject {

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
}
