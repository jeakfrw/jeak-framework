package de.fearnixx.jeak.service;

import java.util.Optional;

/**
 * The service manager allows plugins to easily access framework services.
 * The service manager also allows plugins to register framework services.
 *
 * Created by MarkL4YG on 20.06.17.
 */
public interface IServiceManager {

    /**
     * Register a service.
     * @param svcClass The class the service provides
     * @param svc The service instance
     * @param <T> Your service type
     */
    <T> void registerService(Class<T> svcClass, T svc);

    /**
     * Optionally get a service
     *
     * Optional is empty if the service does not exist OR is no instance of T
     * @param svcClass The desired service class
     * @param <T> The desired service type
     * @return {@link Optional<T>} An optional representing the result
     */
    <T> Optional<T> provide(Class<T> svcClass);

    /**
     * Get a service.
     *
     * This method performs no checks! The following may happen:
     * * Your desired service is returned
     * * {@code null} is returned
     * * A {@link ClassCastException} is thrown
     * @param svcClass The desired service class
     * @param <T> The desired service type
     * @return The service instance or null
     */
    <T> T provideUnchecked(Class<T> svcClass);
}
