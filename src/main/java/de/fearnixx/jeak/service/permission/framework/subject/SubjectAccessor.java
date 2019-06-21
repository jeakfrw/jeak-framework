package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.except.CircularInheritanceException;
import de.fearnixx.jeak.service.permission.framework.SubjectCache;

import java.util.Comparator;
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

    @Override
    public Optional<IPermission> getPermission(String permission) {
        return getPermissionFromSelf(permission)
                .or(() -> getPermissionFromParents(permission));
    }

    protected abstract Optional<IPermission> getPermissionFromSelf(String permission);

    protected Optional<IPermission> getPermissionFromParents(String permission) {
        return getParents().stream()
                .map(parent -> parent.getPermission(permission))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparing(IPermission::getValue));
    }

    /**
     * @throws CircularInheritanceException if inheritance circularity is detected.
     */
    protected void addMemberCircularityCheck(UUID memberUUID) {
        getParents().stream()
                .map(ISubject::getUniqueID)
                .forEach(parentUID -> {
                    if (parentUID.equals(memberUUID)) {
                        throw new CircularInheritanceException(memberUUID + " is already parent of: " + getUniqueID());
                    }
                });
    }

    public abstract void saveIfModified();

    protected SubjectCache getCache() {
        return permissionSvc;
    }

    public abstract void mergeFrom(SubjectAccessor fromSubject);

    public abstract void invalidate();

    protected abstract void addParent(UUID uniqueID);
}
