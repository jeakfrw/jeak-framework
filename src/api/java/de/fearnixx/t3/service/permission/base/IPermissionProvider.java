package de.fearnixx.t3.service.permission.base;

import java.util.Optional;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public interface IPermissionProvider {

    Optional<IPermission> getPermission(String permSID, String clientUID);
}
