package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3ServerGroupSubject;
import de.fearnixx.jeak.service.permission.teamspeak.TS3UserSubject;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;

import java.util.*;

public class TS3User extends TS3UserHolder {
    private TS3UserSubject ts3PermSubject;

    private UUID frameworkSubject;
    private IPermissionProvider frwPermProvider;

    public void setTs3PermSubject(TS3UserSubject ts3PermSubject) {
        if (this.ts3PermSubject != null) {
            throw new IllegalStateException("#setTs3PermSubject is an unsafe operation and may not be repeated after init!");
        }
        this.ts3PermSubject = ts3PermSubject;
    }

    public void setFrameworkSubjectUUID(UUID frameworkSubject) {
        if (this.frameworkSubject != null) {
            throw new IllegalStateException("#setFrameworkSubject is an unsafe operation and may not be repeated after init!");
        }
        this.frameworkSubject = frameworkSubject;
    }

    public void setFrwPermProvider(IPermissionProvider frwPermProvider) {
        this.frwPermProvider = frwPermProvider;
    }

    // == Framework subject == //


    private ISubject getSubject() {
        return frwPermProvider.getSubject(frameworkSubject)
                .orElseThrow(() -> new IllegalStateException("Framework did not return subject for: " + frameworkSubject));
    }

    @Override
    public UUID getUniqueID() {
        return getSubject().getUniqueID();
    }

    @Override
    public List<IGroup> getParents() {
        return getSubject().getParents();
    }

    @Override
    public boolean hasParent(UUID uniqueID) {
        return getSubject().hasParent(uniqueID);
    }

    @Override
    public List<IPermission> getPermissions() {
        return getSubject().getPermissions();
    }

    @Override
    public boolean hasPermission(String permission) {
        return getSubject().hasPermission(permission);
    }

    @Override
    public Optional<IPermission> getPermission(String permission) {
        return getSubject().getPermission(permission);
    }

    @Override
    public Optional<IPermission> getPermission(String permission, boolean allowTransitive) {
        return getSubject().getPermission(permission, allowTransitive);
    }

    @Override
    public boolean setPermission(String permission, int value) {
        return getSubject().setPermission(permission, value);
    }

    @Override
    public boolean removePermission(String permission) {
        return getSubject().removePermission(permission);
    }


    // == TS3 Subject == //

    @Override
    public List<ITS3ServerGroupSubject> getServerGroups() {
        return ts3PermSubject.getServerGroups(getGroupIDs());
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return ts3PermSubject.getTS3Permission(permSID);
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return ts3PermSubject.getActiveTS3Permission(permSID);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        return ts3PermSubject.assignPermission(permSID, value, permSkip);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip) {
        return ts3PermSubject.assignPermission(permSID, value, permSkip);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value) {
        return ts3PermSubject.assignPermission(permSID, value);
    }

    @Override
    public IQueryRequest revokePermission(String permSID) {
        return ts3PermSubject.revokePermission(permSID);
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
