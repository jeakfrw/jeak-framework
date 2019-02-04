package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.teamspeak.PropertyKeys.Channel;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by MarkL4YG on 15.06.17.
 */
@SuppressWarnings("ConstantConditions")
public class TS3Channel extends BasicDataHolder implements IChannel {

    private boolean invalidated = false;
    private List<IChannel> children;

    public TS3Channel(){
        super();
        children = new ArrayList<>();
    }

    public void invalidate() {
        clearChildren();
        invalidated = true;
    }

    @Override
    public Integer getID() {
        return Integer.parseInt(getProperty(Channel.ID).get());
    }

    @Override
    public Integer getParent() {
        return Integer.parseInt(getProperty(Channel.PARENT).orElse("0"));
    }

    @Override
    public Integer getOrder() {
        return Integer.parseInt(getProperty(Channel.ORDER).orElse("0"));
    }

    @Override
    public String getName() {
        return getProperty(Channel.NAME).orElse("null");
    }

    @Override
    public String getTopic() {
        return getProperty(Channel.TOPIC).orElse("");
    }

    @Override
    public Boolean isDefault() {
        return Boolean.parseBoolean(getProperty(Channel.FLAG_DEFAULT).orElse("false"));
    }

    @Override
    public Boolean hasPassword() {
        return Boolean.valueOf(getProperty(Channel.FLAG_PASSWORD).orElse("false"));
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
        String perm = getValues().getOrDefault(Channel.FLAG_PERMANENT, "0");
        String semi = getValues().getOrDefault(Channel.FLAG_SEMI_PERMANENT, "0");
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
        return Integer.parseInt(getProperty(Channel.TALK_POWER).orElse("0"));
    }

    @Override
    public Integer getClientCount() {
        return Integer.parseInt(getProperty(Channel.CLIENT_COUNT).orElse("0"));
    }

    @Override
    public Integer getMaxClientCount() {
        return Integer.parseInt(getProperty(Channel.MAX_CLIENTS).orElse("-1"));
    }

    @Override
    public Integer getClientCountBelow() {
        return Integer.parseInt(getProperty(Channel.CLIENT_COUNT_FAMILY).orElse("0"));
    }

    @Override
    public Integer getMaxClientCountBelow() {
        return Integer.parseInt(getProperty(Channel.MAX_CLIENTS_FAMILY).orElse("0"));
    }

    @Override
    public Integer getCodec() {
        return Integer.parseInt(getProperty(Channel.CODEC).orElse("0"));
    }

    @Override
    public Integer getCodecQuality() {
        return Integer.parseInt(getProperty(Channel.QUALITY).orElse("0"));
    }

    public void clearChildren() {
        children.clear();
    }

    @Override
    public List<IChannel> getSubChannels() {
        return Collections.unmodifiableList(children);
    }

    public void addSubChannel(IChannel channel) {
        int id = getID();
        if (channel.getParent() != id)
            throw new IllegalArgumentException(id + ": Channel " + channel.getID() + "is not my child! :" + channel.getParent());
        if (!children.contains(channel)) children.add(channel);
    }

    @Override
    public String toString() {
        return getName() + '/' + getID();
    }
}
