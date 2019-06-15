package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission;
import de.fearnixx.jeak.service.permission.teamspeak.TS3PermissionSubject;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TS3User extends TS3UserHolder {

    private TS3PermissionSubject permSubject;

    public void setPermSubject(TS3PermissionSubject permSubject) {
        if (this.permSubject != null) {
            throw new IllegalStateException("#setPermSubject is an unsafe operation and may not be repeated after init!");
        }
        this.permSubject = permSubject;
    }

    // == Permission subject == //

    @Override
    public UUID getUniqueID() {
        return permSubject.getUniqueID();
    }

    @Override
    public List<IGroup> getParents() {
        return permSubject.getParents();
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
        return permSubject.assignPermission(permSID, value, permSkip);
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

    // == User == //

    @Override
    public IQueryRequest addServerGroup(Integer... serverGroupIds) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.SERVER_GROUP.SERVERGROUP_ADD_CLIENT)
                .addKey("cldbid", this.getClientDBID());
        Arrays.stream(serverGroupIds).forEach(id -> {
            queryBuilder.addKey("sgid", id).commitChainElement();
        });
        return queryBuilder.build();
    }

    @Override
    public IQueryRequest removeServerGroup(Integer... serverGroupIds) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.SERVER_GROUP.SERVERGROUP_DEL_CLIENT)
                .addKey("cldbid", this.getClientDBID());
        Arrays.stream(serverGroupIds).forEach(id -> {
            queryBuilder.addKey("sgid", id).commitChainElement();
        });
        return queryBuilder.build();
    }

    @Override
    public IQueryRequest setChannelGroup(Integer channelId, Integer channelGroupId) {
        return IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_SET_CHANNEL_GROUP)
                .addKey("cldbid", this.getClientDBID())
                .addKey(PropertyKeys.Channel.ID, channelId)
                .addKey("cgid", channelGroupId)
                .build();
    }
}
