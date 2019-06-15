package de.fearnixx.jeak.service.permission.base;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides permission information about clients.
 */
public interface IPermissionProvider {

    /**
     * Returns the stored information about this permission for the specified client.
     */
    Optional<IPermission> getPermission(String permSID, String clientTS3UniqueID);

    /**
     * Returns the stored information about this permission for the specified user/profile.
     */
    Optional<IPermission> getPermission(String permSID, UUID subjectUniqueID);
}
