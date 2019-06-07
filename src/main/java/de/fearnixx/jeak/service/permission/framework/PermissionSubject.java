package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IPermission;

import java.util.Optional;
import java.util.UUID;

public abstract class PermissionSubject {

    private UUID subjectUUID;

    public PermissionSubject(UUID subjectUUID) {
        this.subjectUUID = subjectUUID;
    }

    public abstract Optional<IPermission> getPermission(String permSID);

    public abstract SubjectType getType();

    public UUID getUniqueID() {
        return subjectUUID;
    }
}
