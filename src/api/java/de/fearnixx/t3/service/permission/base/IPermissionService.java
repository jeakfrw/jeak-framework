package de.fearnixx.t3.service.permission.base;

import java.util.Optional;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public interface IPermissionService {

    Optional<IPermissionProvider> provide(String systemID);

    void registerProvider(String systemID, IPermissionProvider provider);
}
