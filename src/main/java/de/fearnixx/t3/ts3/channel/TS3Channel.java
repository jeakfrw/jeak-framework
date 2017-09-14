package de.fearnixx.t3.ts3.channel;

import de.fearnixx.t3.ts3.keys.PropertyKeys.Channel;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.query.QueryMessageObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by MarkL4YG on 15.06.17.
 */
@SuppressWarnings("ConstantConditions")
public class TS3Channel extends QueryMessageObject implements IChannel {

    boolean invalidated = false;
    private List<IChannel> children;

    public TS3Channel(){
        super();
        children = new ArrayList<>();
    }

    @Override
    public void copyFrom(IQueryMessageObject obj) {
        synchronized (super.lock) {
            obj.getKeys().forEach(k -> {
                super.properties.put(k, obj.getProperty(k).get());
            });
        }
    }

    public void invalidate() {
        synchronized (super.lock) {
            clearChildren();
            invalidated = true;
        }
    }

    @Override
    public int getID() {
        return Integer.parseInt(getProperty(Channel.ID).get());
    }

    @Override
    public int getParent() {
        return Integer.parseInt(getProperty(Channel.PARENT).orElse("0"));
    }

    @Override
    public int getOrder() {
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
    public boolean isDefault() {
        return Boolean.parseBoolean(getProperty(Channel.FLAG_DEFAULT).orElse("false"));
    }

    @Override
    public boolean hasPassword() {
        return Boolean.valueOf(getProperty(Channel.FLAG_PASSWORD).orElse("false"));
    }

    /**
     * @implNote If the type changes from channel to spacer or from spacer to channel the object will be abandoned
     */
    @Override
    public boolean isSpacer() {
        return this instanceof ISpacer;
    }

    @Override
    public ChannelPersistence getPersistence() {
        synchronized (super.lock) {
            if (invalidated)
                return ChannelPersistence.DELETED;
        }
        synchronized (super.lock) {
            String perm = super.properties.getOrDefault(Channel.FLAG_PERMANENT, "0");
            String semi = super.properties.getOrDefault(Channel.FLAG_SEMI_PERMANENT, "0");
            if ("0".equals(perm) && "0".equals(semi)) {
                return ChannelPersistence.TEMPORARY;
            }
            if ("1".equals(perm))
                return ChannelPersistence.PERMANENT;
            if ("1".equals(semi))
                return ChannelPersistence.SEMI_PERMANENT;
            throw new RuntimeException("A channel has reached invalid persistence! Maybe a plugin is editing channels wrongly.");
        }
    }

    @Override
    public int getTalkPower() {
        return Integer.parseInt(getProperty(Channel.TALK_POWER).orElse("0"));
    }

    @Override
    public int getClientCount() {
        return Integer.parseInt(getProperty(Channel.CLIENT_COUNT).orElse("0"));
    }

    @Override
    public int getMaxClientCount() {
        return Integer.parseInt(getProperty(Channel.MAX_CLIENTS).orElse("-1"));
    }

    @Override
    public int getClientCountBelow() {
        return Integer.parseInt(getProperty(Channel.CLIENT_COUNT_FAMILY).orElse("0"));
    }

    @Override
    public int getMaxClientCountBelow() {
        return Integer.parseInt(getProperty(Channel.MAX_CLIENTS_FAMILY).orElse("0"));
    }

    @Override
    public int getCodec() {
        return Integer.parseInt(getProperty(Channel.CODEC).orElse("0"));
    }

    @Override
    public int getCodecQuality() {
        return Integer.parseInt(getProperty(Channel.QUALITY).orElse("0"));
    }

    public void clearChildren() {
        synchronized (super.lock) {
            children.clear();
        }
    }

    @Override
    public List<IChannel> getSubChannels() {
        synchronized (super.lock) {
            return Collections.unmodifiableList(children);
        }
    }

    public void addSubChannel(IChannel channel) {
        int id = getID();
        if (channel.getParent() != id)
            throw new IllegalArgumentException(id + ": Channel " + channel.getID() + "is not my child! :" + channel.getParent());
        synchronized (super.lock) {
            if (!children.contains(channel)) children.add(channel);
        }
    }
}
