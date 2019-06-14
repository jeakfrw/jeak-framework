package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IPermission;

import java.util.UUID;

public class FrameworkPermission implements IPermission {

    private final UUID subjectUUID;
    private final String permSID;
    private final int value;

    public FrameworkPermission(UUID subjectUUID, String permSID, int value) {
        this.subjectUUID = subjectUUID;
        this.permSID = permSID;
        this.value = value;
    }

    public UUID getSubjectUUID() {
        return subjectUUID;
    }

    @Override
    public String getSID() {
        return permSID;
    }

    @Override
    public String getSystemID() {
        return FrameworkPermissionService.SYSTEM_ID;
    }

    @Override
    public String getFullyQualifiedID() {
        return FrameworkPermissionService.SYSTEM_ID + ':' + getSID();
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
