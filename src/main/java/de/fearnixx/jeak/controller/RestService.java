package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.connection.HttpServer;
import de.fearnixx.jeak.controller.controller.ControllerContainer;
import de.fearnixx.jeak.controller.interfaces.IRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestService implements IRestService {
    private static final Logger logger = LoggerFactory.getLogger(RestService.class);
    private static final String REST_API_ENDPOINT = "/api";
    private HttpServer httpServer;

    private RestControllerManager restControllerManager;

    public RestService(RestControllerManager restControllerManager) {
        this.httpServer = new HttpServer();
        this.restControllerManager = restControllerManager;
    }

    public void someStuff() {
        Map<Class<?>, Object> restControllers = restControllerManager.provideAll();
        List<ControllerContainer> controllers = restControllers.values().stream()
                .map(ControllerContainer::new)
                .collect(Collectors.toList());
        controllers.forEach(this::registerMethods);
    }

    private void registerMethods(ControllerContainer controllerContainer) {
        httpServer.registerController(controllerContainer);
    }
}
