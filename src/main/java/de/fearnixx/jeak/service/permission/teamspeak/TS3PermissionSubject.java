package de.fearnixx.jeak.service.permission.teamspeak;

public abstract class TS3PermissionSubject implements ITS3Subject {

    private ITS3PermissionProvider permissionProvider;

    public TS3PermissionSubject(ITS3PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    protected ITS3PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }
}
