package de.fearnixx.jeak.service.permission.base;

import java.util.Optional;

/**
 *
 */
public interface IPermissionProvider {

    Optional<IPermission> getPermission(String permSID, String clientUID);
}
