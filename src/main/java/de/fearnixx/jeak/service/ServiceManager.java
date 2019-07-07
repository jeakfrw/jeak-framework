package de.fearnixx.jeak.service;

import de.fearnixx.jeak.reflect.FrameworkService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@FrameworkService(serviceInterface = IServiceManager.class)
public class ServiceManager implements IServiceManager {

    private final Map<Class<?>, Object> services;

    public ServiceManager() {
        services = new HashMap<>();
    }

    @Override
    public <T> void registerService(Class<T> svcClass, T svc) {
        services.put(svcClass, svc);
    }

    @Override
    public <T> T provideUnchecked(Class<T> svcClass) {
        return svcClass.cast(services.get(svcClass));
    }

    @Override
    public <T> Optional<T> provide(Class<T> svcClass) {
        Object svc = services.getOrDefault(svcClass, null);
        if (svc != null && svcClass.isAssignableFrom(svc.getClass())) {
            return Optional.of(svcClass.cast(svc));
        }
        return Optional.empty();
    }
}
