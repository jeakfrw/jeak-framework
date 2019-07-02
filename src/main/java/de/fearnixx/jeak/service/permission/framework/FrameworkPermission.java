package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IPermission;

public class FrameworkPermission implements IPermission {

    private final String permSID;
    private final int value;

    public FrameworkPermission(String permSID, int value) {
        this.permSID = permSID;
        this.value = value;
    }

    @Override
    public String getSID() {
        return permSID;
    }

    @Override
    public String getSystemID() {
        return InternalPermissionProvider.SYSTEM_ID;
    }

    @Override
    public String getFullyQualifiedID() {
        return InternalPermissionProvider.SYSTEM_ID + ':' + getSID();
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
