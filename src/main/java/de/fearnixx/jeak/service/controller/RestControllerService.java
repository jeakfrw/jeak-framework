package de.fearnixx.jeak.service.controller;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.controller.connection.ControllerRequestVerifier;
import de.fearnixx.jeak.service.controller.connection.HttpServer;
import de.fearnixx.jeak.service.controller.connection.RestConfiguration;
import de.fearnixx.jeak.service.controller.controller.ControllerContainer;
import de.fearnixx.jeak.service.controller.controller.SparkAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@FrameworkService(serviceInterface = IRestControllerService.class)
public class RestControllerService implements IRestControllerService {
    private final Map<Class<?>, Object> controllers;
    private HttpServer httpServer;
    private ControllerRequestVerifier connectionVerifier;
    private RestConfiguration restConfiguration;

    @Inject
    private IInjectionService injectionService;

    public RestControllerService() {
        this(new HashMap<>());
    }

    public RestControllerService(Map<Class<?>, Object> controllers) {
        this.connectionVerifier = new ControllerRequestVerifier();
        this.restConfiguration = new RestConfiguration();
        this.controllers = controllers;
        this.httpServer = new SparkAdapter(connectionVerifier, restConfiguration);
    }

    @Listener
    public void onPreInt(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        injectionService.injectInto(connectionVerifier);
        injectionService.injectInto(restConfiguration);
        restConfiguration.loadConfig();
        injectionService.injectInto(httpServer);
        httpServer.start();
    }


    @Override
    public <T> void registerController(Class<T> cntrlrClass, T restController) {
        ControllerContainer controllerContainer = new ControllerContainer(restController);
        if (!doesControllerAlreadyExist(restController)) {
            controllers.put(cntrlrClass, restController);
            httpServer.registerController(controllerContainer);
        } else {
            throw new RegisterControllerException("There is already a controller with the same endpoint");
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

    private <T> boolean doesControllerAlreadyExist(T restController) {
        Class<?> controllerClass = restController.getClass();
        if (controllers.containsKey(controllerClass)) {
            return false;
        }
        return controllers.keySet().stream()
                .filter(aClass -> extractPluginId(aClass).equals(extractPluginId(controllerClass)))
                .anyMatch(aClass -> extractControllerName(aClass).equals(extractControllerName(controllerClass)));
    }

    private String extractControllerName(Class<?> clazz) {
        return clazz.getAnnotation(RestController.class).endpoint();
    }

    private String extractPluginId(Class<?> clazz) {
        return clazz.getAnnotation(RestController.class).pluginId();
    }
}
