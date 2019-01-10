package de.fearnixx.jeak.service.permission.base;

import java.util.Optional;

/**
 *
 * @auth MarkL4YG
 */
public interface IPermissionService {

    Optional<IPermissionProvider> provide(String systemID);

    void registerProvider(String systemID, IPermissionProvider provider);
}
