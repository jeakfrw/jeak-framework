package de.fearnixx.jeak.service.controller;

import java.util.Map;
import java.util.Optional;

/**
 * The controller manager allows plugins to register services to a specified REST method.
 *
 */
public interface IRestControllerService {

    /**
     * Registers a new REST controller to the controller manager.
     *
     * @param cntrlrClass    The class the controller provides.
     * @param restController The controller to be registered.
     * @param <T>            The Type of the service.
     */
    <T> void registerController(Class<T> cntrlrClass, T restController);

    /**
     * Optionally get a controller of the specified class.
     *
     * @param cntrlrClass The desired controller class.
     * @param <T> The desired controller type.
     * @return An {@link Optional<T>} representing the result.
     */
    <T> Optional<T> provide(Class<T> cntrlrClass);

    /**
     * Get a controller.
     * <p>
     * This method performs no checks!
     *
     * @param cntrlrClass The desired service class.
     * @param <T> The desired controller type.
     * @return Either the controller instance,
     * or {@core null} and a {@link ClassCastException} is thrown.
     */
    <T> T provideUnchecked(Class<T> cntrlrClass);

    /**
     * Provide all the registered controllers.
     *
     * @return A {@link Map<Class<?>, Object>} representing the controller.
     */
    Map<Class<?>, Object> provideAll();
}
