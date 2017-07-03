package de.fearnixx.t3.service;

import java.util.Optional;

/**
 * Created by MarkL4YG on 20.06.17.
 */
public interface IServiceManager {

    <T> void registerService(Class<T> svcClass, T svc);

    <T> Optional<T> provide(Class<T> svcClass);

    <T> T provideUnchecked(Class<T> svcClass);
}
