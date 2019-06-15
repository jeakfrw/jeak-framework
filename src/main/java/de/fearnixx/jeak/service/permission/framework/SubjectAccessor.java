package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class SubjectAccessor implements ISubject, IGroup {

    private final UUID subjectUUID;
    private final SubjectCache permissionSvc;

    public SubjectAccessor(UUID subjectUUID, SubjectCache permissionSvc) {
        this.subjectUUID = subjectUUID;
        this.permissionSvc = permissionSvc;
    }

    @Override
    public UUID getUniqueID() {
        return subjectUUID;
    }

    public abstract void saveIfModified();

    public abstract void mergeInto(UUID into);

    protected SubjectCache getCache() {
        return permissionSvc;
    }
}
