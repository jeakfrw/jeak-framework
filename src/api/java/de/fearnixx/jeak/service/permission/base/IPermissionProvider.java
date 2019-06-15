package de.fearnixx.jeak.service.permission.base;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides permission information about clients.
 * Permission providers are - technically - part of the internal API. Plugins should use {@link ISubject} wherever possible.
 * Implementing and registering a custom permission provider will allow other system IDs to be checked.
 * By default, TeamSpeak 3 and the internal permission provider are registered and additional providers may be registered by plugins.
 * @see IPermissionService#registerProvider(String, IPermissionProvider)
 */
public interface IPermissionProvider {

    /**
     * Returns the stored information about this permission for the specified client.
     * Shorthand to avoid uid -> uuid translation in invoker code.
     */
    Optional<IPermission> getPermission(String permSID, String clientTS3UniqueID);

    /**
     * Returns the stored information about this permission for the specified user/profile.
     */
    Optional<IPermission> getPermission(String permSID, UUID subjectUniqueID);

    /**
     * If write-access is <strong>allowed</strong>: Sets the given permission for the given subject to the given value.
     * If write-access is <strong>not allowed</strong>: Logs a warning for illegal access.
     * @apiNote Setting permissions to 0 is explicit and should not be interpreted as a method to remove permissions.
     * @implNote <strong>DO NOT THROW</strong> an exception for when no write access is intended
     *           as this breaks the ability to replace the provider with one that has write access.
     */
    void setPermission(String permSID, UUID subjectUniqueID, int value);

    /**
     * If write-access is <strong>allowed</strong>: Removes the given permission from the given subject.
     * If write-access is <strong>not allowed</strong>: Logs a warning for illegal access.
     * @implNote <strong>DO NOT THROW</strong> an exception for when no write access is intended
     *           as this breaks the ability to replace the provider with one that has write access.
     */
    void removePermission(String permSID, UUID subjectUniqueID);
}
