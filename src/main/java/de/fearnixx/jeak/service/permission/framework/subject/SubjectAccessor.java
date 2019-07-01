package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.except.CircularInheritanceException;
import de.fearnixx.jeak.service.permission.framework.SubjectCache;
import de.fearnixx.jeak.service.permission.framework.index.SubjectIndex;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SubjectAccessor implements ISubject, IGroup {

    private static final Logger logger = LoggerFactory.getLogger(SubjectAccessor.class);

    private final UUID subjectUUID;
    private final SubjectCache permissionSvc;
    private final SubjectIndex subjectIndex;

    @Inject
    private IProfileService profileService;

    @Inject
    private IUserService userService;

    public SubjectAccessor(UUID subjectUUID, SubjectCache permissionSvc, SubjectIndex subjectIndex) {
        this.subjectUUID = subjectUUID;
        this.permissionSvc = permissionSvc;
        this.subjectIndex = subjectIndex;
    }

    @Override
    public UUID getUniqueID() {
        return subjectUUID;
    }

    @Override
    public Optional<IPermission> getPermission(String permission) {
        return getPermission(permission, true);
    }

    @Override
    public Optional<IPermission> getPermission(String permission, boolean allowTransitive) {
        if (allowTransitive) {
            return getPermissionFromSelf(permission)
                    .or(() -> getPermissionFromParents(permission))
                    .or(() -> getPermissionFromLinkedTS(permission));
        } else {
            return getPermissionFromSelf(permission);
        }
    }

    private Optional<IPermission> getPermissionFromLinkedTS(String permission) {
        final Optional<IUserProfile> optProfile = profileService.getProfile(getUniqueID());
        final Map<UUID, String> linkedGroups = new HashMap<>();

        if (optProfile.isEmpty()) {
            logger.debug("Cannot check for ts3-linked groups as profile does not exist: {}", getUniqueID());
            return Optional.empty();
        }
        final IUserProfile profile = optProfile.get();
        profile.getLinkedIdentities(IUserIdentity.SERVICE_TEAMSPEAK)
                .stream()
                .map(IUserIdentity::identity)
                .map(userService::findUserByUniqueID)
                .filter(results -> {
                    if (results.size() > 1) {
                        logger.warn("Multiple matches for #findUserByUniqueID: Dropping result for: {}", results.get(0).getClientUniqueID());
                    }
                    return results.size() == 1;
                })
                .map(r -> r.get(0))
                .map(IUser::getGroupIDs)
                .forEach(ts3GroupIDs -> {
                    ts3GroupIDs.stream()
                            .map(subjectIndex::getGroupsLinkedTo)
                            .filter(results -> !results.isEmpty())
                            .forEach(result -> result.forEach(res -> linkedGroups.put(res, "")));
                });

        @SuppressWarnings("UnnecessaryLocalVariable")
        final Optional<IPermission> maxAssignedPerm = linkedGroups.keySet()
                .stream()
                .map(getCache()::getSubject)
                .map(subject -> subject.getPermission(permission))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparing(IPermission::getValue));

        return maxAssignedPerm;
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
        return subjectIndex.getMembersOf(getUniqueID());
    }

    @Override
    public boolean addMember(UUID uuid) {
        subjectIndex.addParent(getUniqueID(), uuid);
        return true;
    }

    @Override
    public boolean addMember(ISubject subject) {
        return addMember(subject.getUniqueID());
    }

    @Override
    public List<IGroup> getParents() {
        return subjectIndex.getParentsOf(getUniqueID())
                .stream()
                .map(getCache()::getSubject)
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
        subjectIndex.linkServerGroup(this, serverGroupID);
        return true;
    }

    public abstract void saveIfModified();

    protected SubjectCache getCache() {
        return permissionSvc;
    }

    public abstract void mergeFrom(SubjectAccessor fromSubject);

    public abstract void invalidate();
}
