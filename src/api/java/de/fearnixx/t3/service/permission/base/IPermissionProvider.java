package de.fearnixx.t3.service.permission.base;

import java.util.Optional;

/**
 *
 * @author MarkL4YG
 */
public interface IPermissionProvider {

    Optional<IPermission> getPermission(String permSID, String clientUID);
}
