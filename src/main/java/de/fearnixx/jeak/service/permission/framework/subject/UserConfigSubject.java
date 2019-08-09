package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.framework.InternalPermissionProvider;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.mlessmann.confort.api.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserConfigSubject extends ConfigSubject {

    private static final Logger logger = LoggerFactory.getLogger(UserConfigSubject.class);

    @Inject
    private IUserService userService;

    private final IUserProfile userProfile;

    public UserConfigSubject(IUserProfile userProfile, IConfig configRef, InternalPermissionProvider provider) {
        super(userProfile.getUniqueId(), configRef, provider);
        this.userProfile = userProfile;
    }

    @Override
    public Optional<IPermission> getPermission(String permission, boolean allowTransitive) {
        return super.getPermission(permission, allowTransitive)
                .or(() -> getLinkedPermissions(permission, allowTransitive));
    }

    protected Optional<IPermission> getLinkedPermissions(String permission, boolean allowTransitive) {
        if (allowTransitive) {
            List<IGroup> linkedGroups = new LinkedList<>();
            final List<IUserIdentity> linkedIdentities =
                    userProfile.getLinkedIdentities(IUserIdentity.SERVICE_TEAMSPEAK);
            if (linkedIdentities.isEmpty()) {
                throw new IllegalStateException("No identities linked to profile: " + userProfile.getUniqueId());
            }
            linkedIdentities
                    .stream()
                    .map(IUserIdentity::identity)
                    .map(userService::findUserByUniqueID)
                    .filter(results -> {
                        if (!results.isEmpty()) {
                            return true;
                        } else {
                            logger.warn("No user found for profile: {}", getUniqueID());
                            return false;
                        }
                    })
                    .forEach(results -> {
                        logger.debug("Found users: {}", results);
                        results.stream()
                                .map(IUser::getGroupIDs)
                                .forEach(grp -> grp.stream()
                                        .map(getProvider()::getGroupsLinkedToServerGroup)
                                        .forEach(c -> {
                                            logger.debug("-> Found linked groups: {}", c);
                                            linkedGroups.addAll(c);
                                        }));
                    });

            @SuppressWarnings("UnnecessaryLocalVariable") final Optional<IPermission> optMax = linkedGroups.stream()
                    .map(grp -> grp.getPermission(permission))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(Comparator.comparing(IPermission::getValue));

            return optMax;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean addMember(ISubject subject) {
        throw new UnsupportedOperationException("Users may not be parents of other subjects!");
    }

    @Override
    public boolean addMember(UUID uuid) {
        throw new UnsupportedOperationException("Users may not be parents of other subjects!");
    }
}
