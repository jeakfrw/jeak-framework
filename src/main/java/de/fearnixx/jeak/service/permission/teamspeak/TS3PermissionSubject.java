package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TS3PermissionSubject implements ITS3Subject {

    private final Integer myId;
    private ITS3PermissionProvider permissionProvider;

    public TS3PermissionSubject(Integer myId, ITS3PermissionProvider permissionProvider) {
        this.myId = myId;
        this.permissionProvider = permissionProvider;
    }

    protected Integer getMyId() {
        return myId;
    }

    protected ITS3PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return permissionProvider.getClientPermission(myId, permSID);
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return Optional.empty();
    }

    @Override
    public List<ITS3Group> getServerGroups() {
        // TODO: Implement support for TS3 server groups as ITS3Groups
        return Collections.emptyList();
    }

    @Override
    public ITS3Group getChannelGroup() {
        // FIXME: Implement support for TS3 channel groups as ITS3Groups
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip) {
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value) {
        return null;
    }

    @Override
    public IQueryRequest unassignPermission(String permSID) {
        return null;
    }
}
