package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.interfaces.IRestControllerManager;
import java.util.Map;
import java.util.Optional;

public class RestControllerManager implements IRestControllerManager {
    private final Map<Class<?>, Object> controllers;

    public RestControllerManager(Map<Class<?>, Object> controllers) {
        this.controllers = controllers;
    }

    @Override
    public <T> void registerController(Class<T> cntrlrClass, T restController) {
        controllers.put(cntrlrClass, restController);
    }

    @Override
    public <T> Optional<T> provide(Class<T> cntrlrClass) {
        Object cntrlr = controllers.getOrDefault(cntrlrClass, null);
        return Optional.ofNullable((T) cntrlr);
    }

    @Override
    public <T> T provideUnchecked(Class<T> cntrlrClass) {
        return (T) controllers.get(cntrlrClass);
    }

    @Override
    public Map<Class<?>, Object> provideAll() {
        return controllers;
    }

}
