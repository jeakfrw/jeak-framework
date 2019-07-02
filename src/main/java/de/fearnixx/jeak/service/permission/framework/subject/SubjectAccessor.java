package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.except.CircularInheritanceException;
import de.fearnixx.jeak.service.permission.framework.InternalPermissionProvider;
import de.fearnixx.jeak.service.permission.framework.index.SubjectIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class SubjectAccessor implements ISubject, IGroup {

    private static final Logger logger = LoggerFactory.getLogger(SubjectAccessor.class);

    private final UUID uniqueID;
    private final InternalPermissionProvider provider;

    public SubjectAccessor(UUID uniqueID, InternalPermissionProvider provider) {
        this.uniqueID = uniqueID;
        this.provider = provider;
    }

    @Override
    public UUID getUniqueID() {
        return uniqueID;
    }

    @Override
    public Optional<IPermission> getPermission(String permission) {
        return getPermission(permission, true);
    }

    @Override
    public Optional<IPermission> getPermission(String permission, boolean allowTransitive) {
        if (allowTransitive) {
            return getPermissionFromSelf(permission)
                    .or(() -> getPermissionFromParents(permission));
        } else {
            return getPermissionFromSelf(permission);
        }
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
     * FIXME: Actually implement in {@link SubjectIndex}!
     *
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
        return getProvider().getIndex().getMembersOf(getUniqueID());
    }

    @Override
    public boolean addMember(UUID uuid) {
        getProvider().getIndex().addParent(getUniqueID(), uuid);
        return true;
    }

    @Override
    public boolean addMember(ISubject subject) {
        return addMember(subject.getUniqueID());
    }

    @Override
    public List<IGroup> getParents() {
        return getProvider().getIndex().getParentsOf(getUniqueID())
                .stream()
                .map(getProvider().getCache()::getSubject)
                .map(IGroup.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasParent(UUID uniqueID) {
        return getParents()
                .stream()
                .anyMatch(grp -> grp.getUniqueID().equals(uniqueID));
    }

    @Override
    public boolean linkServerGroup(int serverGroupID) {
        getProvider().getIndex().linkServerGroup(this, serverGroupID);
        return true;
    }

    public abstract void saveIfModified();

    public InternalPermissionProvider getProvider() {
        return provider;
    }

    public abstract void mergeFrom(SubjectAccessor fromSubject);

    public abstract void invalidate();
}
