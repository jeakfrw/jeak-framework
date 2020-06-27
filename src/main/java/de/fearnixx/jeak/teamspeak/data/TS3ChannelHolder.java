package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class TS3ChannelHolder extends BasicDataHolder implements IChannel {

    private static final Logger logger = LoggerFactory.getLogger(TS3Channel.class);

    private boolean invalidated = false;
    private final List<TS3ChannelHolder> children;
    private TS3ChannelHolder sortAfter;

    public TS3ChannelHolder(){
        super();
        children = new ArrayList<>();
    }

    public void invalidate() {
        clearChildren();
        invalidated = true;
    }

    @Override
    public Integer getID() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.ID)
                .orElseThrow(() -> new ConsistencyViolationException("Channel is missing ID!")));
    }

    @Override
    public Integer getParent() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.PARENT).orElse("0"));
    }

    @Override
    public Integer getOrder() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.ORDER).orElse("0"));
    }

    @Override
    public String getName() {
        return getProperty(PropertyKeys.Channel.NAME).orElse("null");
    }

    @Override
    public String getTopic() {
        return getProperty(PropertyKeys.Channel.TOPIC).orElse("");
    }

    @Override
    public Boolean isDefault() {
        return Boolean.parseBoolean(getProperty(PropertyKeys.Channel.FLAG_DEFAULT).orElse("false"));
    }

    @Override
    public Boolean hasPassword() {
        return Boolean.valueOf(getProperty(PropertyKeys.Channel.FLAG_PASSWORD).orElse("false"));
    }

    /**
     * @implNote If the type changes from channel to spacer or from spacer to channel the object will be abandoned
     */
    @Override
    public Boolean isSpacer() {
        return this instanceof ISpacer;
    }

    @Override
    public ChannelPersistence getPersistence() {
        if (invalidated)
            return ChannelPersistence.DELETED;
        String perm = getValues().getOrDefault(PropertyKeys.Channel.FLAG_PERMANENT, "0");
        String semi = getValues().getOrDefault(PropertyKeys.Channel.FLAG_SEMI_PERMANENT, "0");
        if ("0".equals(perm) && "0".equals(semi)) {
            return ChannelPersistence.TEMPORARY;
        }
        if ("1".equals(perm))
            return ChannelPersistence.PERMANENT;
        if ("1".equals(semi))
            return ChannelPersistence.SEMI_PERMANENT;
        throw new ConsistencyViolationException("A channel has reached invalid persistence! Maybe a plugin is editing channels wrongly.");

    }

    @Override
    public Integer getTalkPower() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.TALK_POWER).orElse("0"));
    }

    @Override
    public Integer getClientCount() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.CLIENT_COUNT).orElse("0"));
    }

    @Override
    public Integer getMaxClientCount() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.MAX_CLIENTS).orElse("-1"));
    }

    @Override
    public Integer getClientCountBelow() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.CLIENT_COUNT_FAMILY).orElse("0"));
    }

    @Override
    public Integer getMaxClientCountBelow() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.MAX_CLIENTS_FAMILY).orElse("0"));
    }

    @Override
    public Integer getCodec() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.CODEC).orElse("0"));
    }

    @Override
    public Integer getCodecQuality() {
        return Integer.parseInt(getProperty(PropertyKeys.Channel.QUALITY).orElse("0"));
    }

    public void clearChildren() {
        children.clear();
    }

    @Override
    public List<IChannel> getSubChannels() {
        return Collections.unmodifiableList(children);
    }

    public void addSubChannel(TS3ChannelHolder channel) {
        int id = getID();
        if (channel.getParent() != id)
            throw new IllegalArgumentException(id + ": Channel " + channel.getID() + "is not my child! :" + channel.getParent());
        if (!children.contains(channel)) children.add(channel);
    }

    @Override
    public String toString() {
        return getName() + '/' + getID();
    }

    public void setSortAfterChannel(TS3ChannelHolder channel) {
        logger.trace("Sorting after {}", channel);
        this.sortAfter = channel;
    }

    public int getSortingNumber() {
        int sortNumber = 1;
        if (this.sortAfter != null) {
            sortNumber += this.sortAfter.getSortingNumber();
            sortNumber += this.sortAfter.children.size();
        }
        return sortNumber;
    }

    public void sortChildren() {
        children.sort(Comparator.comparingInt(TS3ChannelHolder::getSortingNumber));
    }
}
