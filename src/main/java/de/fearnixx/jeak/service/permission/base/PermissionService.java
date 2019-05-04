package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.reflect.FrameworkService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkService(serviceInterface = IPermissionService.class)
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
