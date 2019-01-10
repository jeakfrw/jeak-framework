package de.fearnixx.jeak.service.permission.base;

import java.util.Optional;

/**
 *
 * @author MarkL4YG
 */
public interface IPermissionProvider {

    Optional<IPermission> getPermission(String permSID, String clientUID);
}
