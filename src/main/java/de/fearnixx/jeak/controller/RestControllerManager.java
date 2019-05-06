package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.connection.HttpServer;
import de.fearnixx.jeak.controller.controller.ControllerContainer;
import de.fearnixx.jeak.controller.interfaces.IRestControllerManager;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class RestControllerManager implements IRestControllerManager {
    private static final Logger logger = LoggerFactory.getLogger(RestControllerManager.class);
    private final Map<Class<?>, Object> controllers;
    private HttpServer httpServer;

    public RestControllerManager() {
        this.controllers = new HashMap<>();
        this.httpServer = new HttpServer();
    }

    public RestControllerManager(Map<Class<?>, Object> controllers) {
        this.controllers = controllers;
        this.httpServer = new HttpServer();
    }

    @Override
    public <T> void registerController(Class<T> cntrlrClass, T restController) {
        controllers.put(cntrlrClass, restController);
        try {
            httpServer.registerController(new ControllerContainer(restController));
        } catch (Exception e) {
            logger.error("there was an error while registering the controller", e);
        }
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
