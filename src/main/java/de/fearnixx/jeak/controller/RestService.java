package de.fearnixx.jeak.controller;

import com.sun.org.glassfish.gmbal.ParameterNames;
import de.fearnixx.jeak.controller.connection.EndpointBuilder;
import de.fearnixx.jeak.controller.connection.HttpServer;
import de.fearnixx.jeak.controller.connection.RequestMethod;
import de.fearnixx.jeak.controller.interfaces.IRestService;
import de.fearnixx.jeak.controller.reflect.RequestBody;
import de.fearnixx.jeak.controller.reflect.RequestMapping;
import de.fearnixx.jeak.controller.reflect.RequestParam;
import de.fearnixx.jeak.controller.reflect.RestController;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

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
        restControllers.forEach((o, o2) -> registerMethods(o2));
    }

    private void registerMethods(Object o) {
        RequestMethod requestMethod;
        RestController restController = o.getClass().getAnnotation(RestController.class);
        RequestMapping requestMapping;

        for (Method method : o.getClass().getDeclaredMethods()) {
            EndpointBuilder endpointBuilder = new EndpointBuilder();
            endpointBuilder.add(REST_API_ENDPOINT);
            endpointBuilder.add(restController.endpoint());
            requestMapping = method.getAnnotation(RequestMapping.class);
            endpointBuilder.add(requestMapping.endpoint());
            requestMethod = requestMapping.method();
            httpServer.registerMethod(requestMethod, endpointBuilder.build(), generateRoute(o, method));
        }
        o.getClass().getAnnotation(RestController.class);
    }

    private Route generateRoute(Object o, Method method) {
        List<MethodParameter> methodParameterList = getMethodParameters(method);
        return (request, response) -> {
            Object[] methodParameters = new Object[methodParameterList.size()];
            for (MethodParameter methodParameter : methodParameterList) {
                if (methodParameter.hasAnnotation(RequestParam.class)) {
                    methodParameters[methodParameter.getPosition()] = request.params(getNameFromAnnotation(methodParameter));
                } else if (methodParameter.hasAnnotation(RequestBody.class)) {
                    methodParameters[methodParameter.getPosition()] = request.body();
                }
            }
            return method.invoke(o, methodParameters);
        };
    }

    /**
     * retrieve the name from a as {@link RequestParam} annotated value.
     *
     * @param methodParameter
     * @return The name of the annotated variable.
     */
    private String getNameFromAnnotation(MethodParameter methodParameter) {
        Optional<? extends Annotation> optionalAnnotation = methodParameter.getAnnotation(RequestParam.class);
        String annotatedName = null;
        if (optionalAnnotation.isPresent()) {
            annotatedName = ((RequestParam) optionalAnnotation.get()).name();
        }
        return annotatedName;
    }

    private List<MethodParameter> getMethodParameters(Method method) {
        List<MethodParameter> methodParameterList = new ArrayList<>(method.getParameterCount());
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            methodParameterList.add(
                    new MethodParameter(i,
                    parameters[i].getType(),
                    parameters[i].getName(),
                    Arrays.asList(parameters[i].getAnnotations()))
            );
        }
        return methodParameterList;
    }
}
