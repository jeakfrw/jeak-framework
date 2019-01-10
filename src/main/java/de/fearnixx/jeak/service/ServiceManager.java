package de.fearnixx.jeak.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by MarkL4YG on 20.06.17.
 */
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
        return (T) services.get(svcClass);
    }

    @Override
    public <T> Optional<T> provide(Class<T> svcClass) {
        Object svc = services.getOrDefault(svcClass, null);
        if (svc != null && svcClass.isAssignableFrom(svc.getClass())) {
            return Optional.of((T) svc);
        }
        return Optional.empty();
    }
}
