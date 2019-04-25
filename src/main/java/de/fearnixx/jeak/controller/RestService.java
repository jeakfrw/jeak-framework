package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.connection.HttpServer;
import de.fearnixx.jeak.controller.connection.RequestMethod;
import de.fearnixx.jeak.controller.interfaces.IRestService;
import de.fearnixx.jeak.controller.reflect.RequestMapping;
import de.fearnixx.jeak.controller.reflect.RestController;
import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestService implements IRestService {
    private static final Logger logger = LoggerFactory.getLogger(RestService.class);
    private static final String REST_API_ENDPOINT = "/api";
    private HttpServer httpServer;

    private RestControllerManager restControllerManager;

    public RestService(HttpServer httpServer, RestControllerManager restControllerManager) {
        this.httpServer = httpServer;
        this.restControllerManager = restControllerManager;
    }

    public void someStuff() {
        Map<Class<?>, Object> restControllers = restControllerManager.provideAll();
        restControllers.forEach(this::registerMethods);
    }

    private void registerMethods(Class<?> clazz, Object o) {
        RequestMethod requestMethod;
        StringBuilder endpoint = new StringBuilder(REST_API_ENDPOINT);
        RequestMapping requestMapping;
        RestController restController = o.getClass().getAnnotation(RestController.class);
        endpoint.append(restController.name());

        for (Method method : o.getClass().getDeclaredMethods()) {
            requestMapping = method.getAnnotation(RequestMapping.class);
            endpoint.append(requestMapping.endpoint());
            requestMethod = requestMapping.method();
            httpServer.registerMethod(requestMethod, endpoint.toString(), method);
        }
        o.getClass().getAnnotation(RestController.class);
    }

}
