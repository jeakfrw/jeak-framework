package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.profile.IUserProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subjects are "things" that can have or lack permissions.
 * At the moment, these are users (and thus, clients) and groups.
 */
public interface ISubject {

    /**
     * The UUID of the subject.
     * @implNote for clients that own a {@link IUserProfile}, this will also be the profile identifier.
     */
    UUID getUniqueID();

    /**
     * Parents of this subject from which permissions are inherited.
     * @apiNote TeamSpeak groups are not represented in this
     */
    List<IGroup> getParents();

    /**
     * Whether or not the subject has a positive value set for the provided permission.
     * @apiNote If a prefix ("something:") is given, the corresponding permission provider will be used.
     *          Otherwise {@link IPermissionService#getFrameworkProvider()} is used.
     */
    boolean hasPermission(String permission);

    /**
     * @see #hasPermission(String) but with more control over whether or not the permission is set and what exact value is defined.
     */
    Optional<IPermission> getPermission(String permission);
}
