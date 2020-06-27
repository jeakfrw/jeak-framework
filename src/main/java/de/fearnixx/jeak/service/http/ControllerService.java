package de.fearnixx.jeak.service.http;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.reflect.http.RestController;
import de.fearnixx.jeak.service.http.connection.ControllerRequestVerifier;
import de.fearnixx.jeak.service.http.connection.HttpServer;
import de.fearnixx.jeak.service.http.connection.RestConfiguration;
import de.fearnixx.jeak.service.http.controller.ControllerContainer;
import de.fearnixx.jeak.service.http.controller.IncapableDummyAdapter;
import de.fearnixx.jeak.service.http.controller.SparkAdapter;
import de.fearnixx.jeak.service.http.exceptions.RegisterControllerException;
import de.fearnixx.jeak.service.http.request.auth.token.TokenAuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@FrameworkService(serviceInterface = IControllerService.class)
public class ControllerService implements IControllerService {

    private final Map<Class<?>, Object> controllers;
    private final HttpServer httpServer;
    private final TokenAuthService tokenAuthService;
    private final RestConfiguration restConfiguration;

    @Inject
    private IInjectionService injectionService;

    public ControllerService() {
        this(new HashMap<>());
    }

    public ControllerService(Map<Class<?>, Object> controllers) {
        this.tokenAuthService = new TokenAuthService();
        this.restConfiguration = new RestConfiguration();
        this.controllers = controllers;
        this.httpServer = IncapableDummyAdapter.EXPERIMENTAL_REST_ENABLED ?
                new SparkAdapter(restConfiguration, tokenAuthService)
                : new IncapableDummyAdapter(restConfiguration);
    }

    @Listener
    public void onPreInt(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        injectionService.injectInto(tokenAuthService);
        injectionService.injectInto(restConfiguration);
        restConfiguration.loadConfig();
        injectionService.injectInto(httpServer);
        httpServer.start();
    }


    @Override
    public <T> void registerController(Class<T> cntrlrClass, T instance) {
        ControllerContainer controllerContainer = new ControllerContainer(instance);
        if (!doesControllerAlreadyExist(instance)) {
            controllers.put(cntrlrClass, instance);
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
        Objects.requireNonNull(cntrlrClass, "Controller class type hint cannot be null!");
        return cntrlrClass.cast( controllers.get(cntrlrClass));
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
