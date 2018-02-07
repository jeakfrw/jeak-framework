package de.fearnixx.t3.service.permission.base;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class PermissionService implements IPermissionService {

    private Map<String, IPermissionProvider> providers = new ConcurrentHashMap<String, IPermissionProvider>();

    @Override
    public Optional<IPermissionProvider> provide(String systemID) {
        return Optional.ofNullable(providers.getOrDefault(systemID, null));
    }

    @Override
    public void registerProvider(String systemID, IPermissionProvider provider) {
        providers.put(systemID, provider);
    }
}
