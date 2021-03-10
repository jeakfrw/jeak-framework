package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class TS3UserHolder extends RawQueryEvent.Message implements IUser {

    // Whether or not having no groups assigned should auto-return an assignment to the default group.
    private static final boolean INHERIT_DEFAULT_GRP_ON_NONE = Main.getProperty("jeak.ts3.inheritDefaultGroup", true);

    private static final Logger logger = LoggerFactory.getLogger(TS3UserHolder.class);

    protected abstract int getDefaultSGID();

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

        if (!s.isBlank()) {
            return Arrays.stream(s.split(","))
                    .filter(sID -> sID != null && !sID.isBlank())
                    .map(sID -> {
                        try {
                            return Integer.parseInt(sID);
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid group ID from TS: {} in {}", sID, this, new ConsistencyViolationException(e));
                            return -1;
                        }
                    })
                    .filter(id -> id > 0)
                    .collect(Collectors.toUnmodifiableList());
        } else {
            // No groups: Apply default group?
            if (INHERIT_DEFAULT_GRP_ON_NONE) {
                return List.of(getDefaultSGID());
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public String toString() {
        return getNickName() + "/db" + getClientDBID();
    }
}
