package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.query.QueryMessageObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by MarkL4YG on 20.06.17.
 */
public class TS3Client extends QueryMessageObject implements IClient {

    boolean invalidated = false;

    public TS3Client(){
        super();
    }

    public void invalidate() {
        synchronized (super.lock) {
            invalidated = true;
        }
    }

    @Override
    public boolean isValid() {
        synchronized (super.lock) {
            return !invalidated;
        }
    }

    @Override
    public String getClientUniqueID() {
        return getProperty(PropertyKeys.Client.UID).get();
    }

    @Override
    public int getClientID() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.ID).get());
    }

    @Override
    public int getClientDBID() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.DBID).get());
    }

    @Override
    public String getNickName() {
        return getProperty(PropertyKeys.Client.NICKNAME).get();
    }

    @Override
    public String getIconID() {
        return getProperty(PropertyKeys.Client.ICON_ID).get();
    }

    @Override
    public PlatformType getPlatform() {
        switch (getProperty(PropertyKeys.Client.PLATFORM).get().toLowerCase()) {
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
        return getProperty(PropertyKeys.Client.VERSION).get();
    }

    @Override
    public ClientType getClientType() {
        return ClientType.values()[Integer.parseInt(getProperty(PropertyKeys.Client.TYPE).get())];
    }

    @Override
    public int getChannelID() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.CHANNEL_ID).get());
    }

    @Override
    public int getChannelGroupID() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.CHANNEL_GROUP).get());
    }

    @Override
    public int getChannelGroupSource() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.CHANNEL_GROUP_SOURCE).get());
    }

    @Override
    public boolean isAway() {
        return getProperty(PropertyKeys.Client.FLAG_AWAY).get().equals("1");
    }

    @Override
    public String getAwayMessage() {
        return getProperty(PropertyKeys.Client.AWAY_MESSAGE).get();
    }

    @Override
    public int getTalkPower() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.TALKPOWER).get());
    }

    @Override
    public boolean isTalking() {
        return getProperty(PropertyKeys.Client.FLAG_TALKING).get().equals("1");
    }

    @Override
    public boolean isTalker() {
        return getProperty(PropertyKeys.Client.FLAG_TALKER).get().equals("1");
    }

    @Override
    public boolean isPrioTalker() {
        return getProperty(PropertyKeys.Client.FLAG_PRIO_TALKER).get().equals("1");
    }

    @Override
    public boolean isCommander() {
        return getProperty(PropertyKeys.Client.FLAG_COMMANDER).get().equals("1");
    }

    @Override
    public boolean isRecording() {
        return getProperty(PropertyKeys.Client.FLAG_RECORDING).get().equals("1");
    }

    @Override
    public boolean hasMic() {
        return getProperty(PropertyKeys.Client.IOIN).get().equals("1");
    }

    @Override
    public boolean hasMicMuted() {
        return getProperty(PropertyKeys.Client.IOIN_MUTED).get().equals("1");
    }

    @Override
    public boolean hasOutput() {
        return getProperty(PropertyKeys.Client.IOOUT).get().equals("1");
    }

    @Override
    public boolean hasOutputMuted() {
        return getProperty(PropertyKeys.Client.IOOUT_MUTED).get().equals("1");
    }

    @Override
    public List<Integer> getGroupIDs() {
        String s = getProperty(PropertyKeys.Client.GROUPS).get();
        String[] sIDs = s.split(",");
        Integer[] ids = new Integer[sIDs.length];
        for (int i = 0; i < sIDs.length; i++) {
            ids[i] = Integer.parseInt(sIDs[i]);
        }
        return Collections.unmodifiableList(Arrays.asList(ids));
    }

    @Override
    public int getIdleTime() {
        return Integer.parseInt(getProperty(PropertyKeys.Client.IDLE_TIME).get());
    }

    @Override
    public long getCreated() {
        return Long.parseLong(getProperty(PropertyKeys.Client.CREATED_TIME).get());
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return LocalDateTime.ofEpochSecond(getCreated(), 0, ZoneOffset.UTC);
    }

    @Override
    public long getLastJoin() {
        return Long.parseLong(getProperty(PropertyKeys.Client.LAST_JOIN_TIME).get());
    }

    @Override
    public LocalDateTime getLastJoinTime() {
        return LocalDateTime.ofEpochSecond(getLastJoin(), 0, ZoneOffset.UTC);
    }
}
