package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.connection.HttpServer;
import de.fearnixx.jeak.controller.connection.RequestMethod;
import de.fearnixx.jeak.controller.interfaces.IRestService;
import de.fearnixx.jeak.controller.reflect.RequestMapping;
import de.fearnixx.jeak.controller.reflect.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

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
        restControllers.forEach(this::registerMethods);
    }

    private void registerMethods(Class<?> clazz, Object o) {
        RequestMethod requestMethod;
        RequestMapping requestMapping;
        RestController restController = o.getClass().getAnnotation(RestController.class);
        String basicEndpoint = REST_API_ENDPOINT + "/" + restController.name();

        for (Method method : o.getClass().getDeclaredMethods()) {
            String fullEndpoint = basicEndpoint;
            requestMapping = method.getAnnotation(RequestMapping.class);
            fullEndpoint+="/"+requestMapping.endpoint();
            requestMethod = requestMapping.method();
            httpServer.registerMethod(requestMethod, fullEndpoint, generateRoute(clazz, method));
        }
        o.getClass().getAnnotation(RestController.class);
    }

    private Route generateRoute(Class<?> clazz, Method method) {
        return (request, response) -> {
            Parameter[] parameters = method.getParameters();
            Object[] methodParams = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                methodParams[i] = request.params(parameters[i].getName());
            }
            return method.invoke(clazz.newInstance(), methodParams);
        };
    }
}
