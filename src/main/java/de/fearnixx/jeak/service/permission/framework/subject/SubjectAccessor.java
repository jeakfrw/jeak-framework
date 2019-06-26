package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.except.CircularInheritanceException;
import de.fearnixx.jeak.service.permission.framework.SubjectCache;
import de.fearnixx.jeak.service.permission.framework.membership.MembershipIndex;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class SubjectAccessor implements ISubject, IGroup {

    private final UUID subjectUUID;
    private final SubjectCache permissionSvc;
    private final MembershipIndex membershipIndex;

    public SubjectAccessor(UUID subjectUUID, SubjectCache permissionSvc, MembershipIndex membershipIndex) {
        this.subjectUUID = subjectUUID;
        this.permissionSvc = permissionSvc;
        this.membershipIndex = membershipIndex;
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
     * FIXME: Actually implement in {@link MembershipIndex}!
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

    @Override
    public List<UUID> getMembers() {
        return membershipIndex.getMembersOf(getUniqueID());
    }

    @Override
    public boolean addMember(UUID uuid) {
        membershipIndex.addParent(getUniqueID(), uuid);
        return true;
    }

    @Override
    public boolean addMember(ISubject subject) {
        return addMember(subject.getUniqueID());
    }

    @Override
    public List<IGroup> getParents() {
        // FIXME: Create specialized checked getter for group subjects
        return membershipIndex.getParentsOf(getUniqueID())
                .stream()
                .map(getCache()::getSubject)
                .map(IGroup.class::cast)
                .collect(Collectors.toList());
    }

    public abstract void saveIfModified();

    protected SubjectCache getCache() {
        return permissionSvc;
    }

    public abstract void mergeFrom(SubjectAccessor fromSubject);

    public abstract void invalidate();
}
