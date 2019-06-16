package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Subject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subjects are "things" that can have or lack permissions.
 * At the moment, these are users (and thus, clients) and groups.
 */
public interface ISubject {

    // === Reading operations === //

    /**
     * The UUID of the subject.
     *
     * @implNote for clients that own a {@link IUserProfile}, this will also be the profile identifier.
     */
    UUID getUniqueID();

    /**
     * Parents of this subject from which permissions are inherited for the given system.
     *
     * @apiNote TeamSpeak groups are not represented in this
     */
    List<IGroup> getParents(String systemID);

    /**
     * Short-hand for {@link #getParents(String)}.
     */
    List<IGroup> getParents();

    /**
     * Whether or not the subject has a positive value set for the provided permission.
     *
     * @apiNote If a prefix ("something:") is given, the corresponding permission provider will be used.
     * Otherwise {@link IPermissionService#getFrameworkProvider()} is used.
     * @implNote It is <strong>not possible</strong> to check TS3 permissions with this. Use {@link ITS3Subject} for that.
     */
    boolean hasPermission(String permission);

    /**
     * @see #hasPermission(String) but with more control over whether or not the permission is set and what exact value is defined.
     */
    Optional<IPermission> getPermission(String permission);

    /**
     * Returns all permissions directly assigned to this subject for the given system ID.
     */
    List<IPermission> getPermissions(String systemId);

    /**
     * @see #getPermissions(String)
     * Returns all permissions assigned to the subject at {@link IPermissionService#getFrameworkProvider()}.
     */
    List<IPermission> getPermissions();

    // === Writing operations === //

    /**
     * Sets the given permission on this subject to the given value.
     *
     * @apiNote Use {@link ITS3Subject#assignPermission(String, int)} and its overloaded signatures for TeamSpeak 3 permissions.
     * @apiNote If a prefix ("something:") is given, the corresponding permission provider will be used.
     * Otherwise {@link IPermissionService#getFrameworkProvider()} is used.
     */
    void setPermission(String permission, int value);

    /**
     * Removes the given permission from this subject.
     *
     * @apiNote This is not the same as setting the permission to 0 as a 0-value can override other values.
     * @apiNote Use {@link ITS3Subject#unassignPermission(String)} for TeamSpeak 3 permissions.
     * @apiNote If a prefix ("something:") is given, the corresponding permission provider will be used.
     * Otherwise {@link IPermissionService#getFrameworkProvider()} is used.
     */
    void removePermission(String permission);
}
