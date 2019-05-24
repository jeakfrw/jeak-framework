package de.fearnixx.jeak.service.controller;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.controller.connection.ControllerRequestVerifier;
import de.fearnixx.jeak.service.controller.connection.HttpServer;
import de.fearnixx.jeak.service.controller.controller.ControllerContainer;
import de.fearnixx.jeak.service.controller.interfaces.IRestControllerService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkService(serviceInterface = IRestControllerService.class)
public class RestControllerService implements IRestControllerService {
    private static final Logger logger = LoggerFactory.getLogger(RestControllerService.class);
    private final Map<Class<?>, Object> controllers;
    private HttpServer httpServer;
    private ControllerRequestVerifier connectionVerifier;

    @Inject
    private IInjectionService injectionService;

    public RestControllerService() {
        this(new HashMap<>());
    }

    public RestControllerService(Map<Class<?>, Object> controllers) {
        connectionVerifier = new ControllerRequestVerifier();
        this.controllers = controllers;
        this.httpServer = new HttpServer(connectionVerifier);
    }

    @Listener
    public void onPreInt(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        injectionService.injectInto(connectionVerifier);
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
