package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 20.06.17.
 */
@SuppressWarnings("ConstantConditions")
public class TS3Client extends RawQueryEvent.Message implements IClient {

    private boolean invalidated = false;

    public TS3Client(){
        super();
    }

    public void invalidate() {
        invalidated = true;
    }

    @Override
    public Boolean isValid() {
        return !invalidated;
    }

    @Override
    public String getClientUniqueID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.UID);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing unique ID")
                    .setSourceObject(this);
        return optProperty.get();
    }

    @Override
    public Integer getClientID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.ID);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing ID")
                    .setSourceObject(this);
        return Integer.parseInt(optProperty.get());
    }

    @Override
    public Integer getClientDBID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.DBID);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing database ID")
                    .setSourceObject(this);
        return Integer.parseInt(optProperty.get());
    }

    @Override
    public String getNickName() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.NICKNAME);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing nickname")
                    .setSourceObject(this);
        return optProperty.get();
    }

    @Override
    public String getIconID() {
        return getProperty(PropertyKeys.Client.ICON_ID).orElse("0");
    }

    @Override
    public PlatformType getPlatform() {
        switch (getProperty(PropertyKeys.Client.PLATFORM).orElse("unknown").toLowerCase()) {
            case "windows": return PlatformType.WINDOWS;
            case "android": return PlatformType.ANDROID;
            case "linux": return PlatformType.LINUX;
            case "ios": return PlatformType.IOS;
            case "os: x": return PlatformType.OSX;
            default: return PlatformType.UNKNOWN;
        }
    }

    @Override
    public String getVersion() {
        return getProperty(PropertyKeys.Client.VERSION).orElse("unknown");
    }

    @Override
    public ClientType getClientType() {
        return ClientType.values()[Integer.parseInt(getProperty(PropertyKeys.Client.TYPE).orElse("0"))];
    }

    @Override
    public Integer getChannelID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.CHANNEL_ID);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing channel ID")
                    .setSourceObject(this);
        return Integer.parseInt(optProperty.get());
    }

    @Override
    public Integer getChannelGroupID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.CHANNEL_GROUP);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing channel group ID")
                    .setSourceObject(this);
        return Integer.parseInt(optProperty.get());
    }

    @Override
    public Integer getChannelGroupSource() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.CHANNEL_GROUP_SOURCE);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client has no channel group source!")
                    .setSourceObject(this);
        return Integer.parseInt(optProperty.get());
    }

    @Override
    public Boolean isAway() {
        return "1".equals(getProperty(PropertyKeys.Client.FLAG_AWAY).orElse(null));
    }

    @Override
    public String getAwayMessage() {
        return getProperty(PropertyKeys.Client.AWAY_MESSAGE).orElse("");
    }

    @Override
    public Integer getTalkPower() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.TALKPOWER).orElse("0"));
    }

    @Override
    public Boolean isTalking() {
        return "1".equals(getProperty(PropertyKeys.Client.FLAG_TALKING).orElse(null));
    }

    @Override
    public Boolean isTalker() {
        return "1".equals(getProperty(PropertyKeys.Client.FLAG_TALKER).orElse(null));
    }

    @Override
    public Boolean isPrioTalker() {
        return "1".equals(getProperty(PropertyKeys.Client.FLAG_PRIO_TALKER).orElse(null));
    }

    @Override
    public Boolean isCommander() {
        return "1".equals(getProperty(PropertyKeys.Client.FLAG_COMMANDER).orElse(null));
    }

    @Override
    public Boolean isRecording() {
        return "1".equals(getProperty(PropertyKeys.Client.FLAG_RECORDING).orElse(null));
    }

    @Override
    public Boolean hasMic() {
        return "1".equals(getProperty(PropertyKeys.Client.IOIN).orElse(null));
    }

    @Override
    public Boolean hasMicMuted() {
        return "1".equals(getProperty(PropertyKeys.Client.IOIN_MUTED).orElse(null));
    }

    @Override
    public Boolean hasOutput() {
        return "1".equals(getProperty(PropertyKeys.Client.IOOUT).orElse(null));
    }

    @Override
    public Boolean hasOutputMuted() {
        return "1".equals(getProperty(PropertyKeys.Client.IOOUT_MUTED).orElse(null));
    }

    @Override
    public List<Integer> getGroupIDs() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.GROUPS);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client has no server groups")
                    .setSourceObject(this);

        String s = optProperty.get();
        String[] sIDs = s.split(",");
        Integer[] ids = new Integer[sIDs.length];
        for (int i = 0; i < sIDs.length; i++) {
            ids[i] = Integer.parseInt(sIDs[i]);
        }
        return Collections.unmodifiableList(Arrays.asList(ids));
    }

    @Override
    public Integer getIdleTime() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.IDLE_TIME).orElse("0"));
    }

    @Override
    public Long getCreated() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.CREATED_TIME);
        if (!optProperty.isPresent())
            throw new ConsistencyViolationException("Client is missing creation timestamp")
                    .setSourceObject(this);
        return Long.parseLong(optProperty.get());
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return LocalDateTime.ofEpochSecond(getCreated(), 0, ZoneOffset.UTC);
    }

    @Override
    public Long getLastJoin() {
        return Long.parseLong(getProperty(PropertyKeys.Client.LAST_JOIN_TIME).orElse("0"));
    }

    @Override
    public LocalDateTime getLastJoinTime() {
        return LocalDateTime.ofEpochSecond(getLastJoin(), 0, ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return getNickName() + '/' + getClientID() + "/db" + getClientDBID();
    }
}
