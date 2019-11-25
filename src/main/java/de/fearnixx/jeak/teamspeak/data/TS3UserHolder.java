package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public abstract class TS3UserHolder extends RawQueryEvent.Message implements IUser {

    public String getClientUniqueID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.UID);
        return optProperty.orElseThrow(
                () -> new ConsistencyViolationException("Client is missing unique ID").setSourceObject(this));
    }

    public Integer getClientDBID() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.DBID);
        return Integer.parseInt(optProperty.orElseThrow(
                () -> new ConsistencyViolationException("Client is missing database ID").setSourceObject(this)));
    }

    public String getNickName() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.NICKNAME);
        return optProperty.orElseThrow(
                () -> new ConsistencyViolationException("Client is missing nickname").setSourceObject(this));
    }

    @Override
    public String getDescription() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.DESCRIPTION);
        return optProperty.orElse("");
    }

    public String getIconID() {
        return getProperty(PropertyKeys.Client.ICON_ID).orElse("0");
    }

    public Long getCreated() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.CREATED_TIME);
        return Long.parseLong(optProperty.orElseThrow(
                () -> new ConsistencyViolationException("Client is missing creation timestamp").setSourceObject(this)));
    }

    public LocalDateTime getCreatedTime() {
        return LocalDateTime.ofEpochSecond(getCreated(), 0, ZoneOffset.UTC);
    }

    public Long getLastJoin() {
        return Long.parseLong(getProperty(PropertyKeys.Client.LAST_JOIN_TIME).orElse("0"));
    }

    public LocalDateTime getLastJoinTime() {
        return LocalDateTime.ofEpochSecond(getLastJoin(), 0, ZoneOffset.UTC);
    }

    @Override
    public List<Integer> getGroupIDs() {
        Optional<String> optProperty = getProperty(PropertyKeys.Client.GROUPS);

        String s = optProperty.orElseThrow(
                () -> new ConsistencyViolationException("Client has no server groups").setSourceObject(this));
        String[] sIDs = s.split(",");
        Integer[] ids = new Integer[sIDs.length];
        for (int i = 0; i < sIDs.length; i++) {
            ids[i] = Integer.parseInt(sIDs[i]);
        }
        return List.of(ids);
    }
}
