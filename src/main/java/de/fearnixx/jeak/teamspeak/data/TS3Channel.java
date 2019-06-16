package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission;
import de.fearnixx.jeak.service.permission.teamspeak.TS3PermissionSubject;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.PropertyKeys.Channel;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by MarkL4YG on 15.06.17.
 */
public class TS3Channel extends TS3ChannelHolder {

    private static final Boolean CHANNEL_MSG_WARNING = Main.getProperty("jeak.checks.channelMsg", true);

    public static final Logger logger = LoggerFactory.getLogger(TS3Channel.class);

    private TS3PermissionSubject permSubject;

    public void setPermSubject(TS3PermissionSubject permSubject) {
        if (this.permSubject != null) {
            throw new IllegalStateException("#setPermSubject is an unsafe operation and may not be repeated after init!");
        }
        this.permSubject = permSubject;
    }

    @Override
    public IQueryRequest sendMessage(String message) {
        if (CHANNEL_MSG_WARNING) {
            logger.warn("Sending commands to channels is not supported at the moment! You will see the message only in the current channel");
        }
        return IQueryRequest.builder()
                .command(QueryCommands.TEXTMESSAGE_SEND)
                .addKey(PropertyKeys.TextMessage.TARGET_ID, this.getID())
                .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.CHANNEL)
                .addKey(PropertyKeys.TextMessage.MESSAGE, message)
                .build();
    }

    @Override
    public IQueryRequest delete() {
        return delete(false);
    }

    @Override
    public IQueryRequest delete(boolean forced) {
        return IQueryRequest.builder()
                .command(QueryCommands.CHANNEL.CHANNEL_DELETE)
                .addKey(Channel.ID, this.getID())
                .addKey("force", forced ? "1" : "0")
                .build();
    }

    @Override
    public IQueryRequest rename(String channelName) {
        return edit(Collections.singletonMap(Channel.NAME, channelName));
    }

    @Override
    public IQueryRequest moveBelow(Integer channelAboveId) {
        return edit(Collections.singletonMap(Channel.ORDER, channelAboveId.toString()));
    }

    @Override
    public IQueryRequest moveInto(Integer channelParentId) {
        return edit(Collections.singletonMap(Channel.PARENT, channelParentId.toString()));
    }

    @Override
    public IQueryRequest edit(Map<String, String> properties) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.CHANNEL.CHANNEL_EDIT)
                .addKey(Channel.ID, this.getID());

        properties.forEach(queryBuilder::addKey);
        return queryBuilder.build();
    }

    // == Permission Subject == //

    @Override
    public UUID getUniqueID() {
        return permSubject.getUniqueID();
    }

    @Override
    public List<IGroup> getServerGroups() {
        return permSubject.getServerGroups();
    }

    @Override
    public boolean hasPermission(String permission) {
        return permSubject.hasPermission(permission);
    }

    @Override
    public Optional<IPermission> getPermission(String permission) {
        return permSubject.getPermission(permission);
    }

    @Override
    public void setPermission(String permission, int value) {
        permSubject.setPermission(permission, value);
    }

    @Override
    public void removePermission(String permission) {
        permSubject.removePermission(permission);
    }


    // == TS3 Subject == //

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return permSubject.getTS3Permission(permSID);
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return permSubject.getActiveTS3Permission(permSID);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        return permSubject.assignPermission(permSID, value, permSkip, permNegated);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip) {
        return permSubject.assignPermission(permSID, value, permSkip);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value) {
        return permSubject.assignPermission(permSID, value);
    }

    @Override
    public IQueryRequest unassignPermission(String permSID) {
        return permSubject.unassignPermission(permSID);
    }
}