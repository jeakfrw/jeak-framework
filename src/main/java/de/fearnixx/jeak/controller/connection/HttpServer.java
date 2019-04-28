package de.fearnixx.jeak.controller.connection;

import de.fearnixx.jeak.controller.controller.ControllerContainer;
import de.fearnixx.jeak.controller.controller.ControllerMethod;
import de.fearnixx.jeak.controller.controller.MethodParameter;
import de.fearnixx.jeak.controller.reflect.RequestBody;
import de.fearnixx.jeak.controller.reflect.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static spark.Spark.*;

/**
 * A wrapper for the http server.
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private int port = 8723;

    public HttpServer() {
        init();
    }

    public HttpServer(int port) {
        this.port = port;
        init();
    }

    public void init() {
        port(port);
    }

    public void registerController(ControllerContainer controllerContainer) {
        path("/api", () -> {
            before("/*", (request, response) -> logger.debug("Received api call"));
            path(controllerContainer.getControllerEndpoint(), () -> {
                controllerContainer.getControllerMethodList().forEach(controllerMethod -> {
                    registerMethod(
                            controllerMethod.getRequestMethod(),
                            controllerMethod.getPath(),
                            generateRoute(controllerContainer, controllerMethod));
                });
            });
        });
    }

    public void registerMethod(RequestMethod httpMethod, String path, Route route) {
        switch (httpMethod) {
            case GET:
                Spark.get(path, route);
                break;
            case PUT:
                Spark.put(path, route);
                break;
            case POST:
                Spark.post(path, route);
                break;
            case PATCH:
                Spark.patch(path, route);
                break;
            case DELETE:
                Spark.delete(path, route);
                break;
            case HEAD:
                Spark.head(path, route);
                break;
        }
    }

    private Route generateRoute(ControllerContainer controllerContainer, ControllerMethod controllerMethod) {
        List<MethodParameter> methodParameterList = controllerMethod.getMethodParameters();
        return (request, response) -> {
            Object[] methodParameters = new Object[methodParameterList.size()];
            for (MethodParameter methodParameter : methodParameterList) {
                if (methodParameter.hasAnnotation(RequestParam.class)) {
                    methodParameters[methodParameter.getPosition()] = request.params(getRequestParamName(methodParameter));
                } else if (methodParameter.hasAnnotation(RequestBody.class)) {
                    methodParameters[methodParameter.getPosition()] = request.body();
                }
            }
            return controllerContainer.invoke(controllerMethod, methodParameters);
        };
    }

    /**
     * Retrieve the name from a {@link RequestParam} annotated value.
     *
     * @param methodParameter
     * @return The name of the annotated variable.
     */
    private String getRequestParamName(MethodParameter methodParameter) {
        Optional<? extends Annotation> optionalAnnotation = methodParameter.getAnnotation(RequestParam.class);
        String annotatedName = null;
        if (optionalAnnotation.isPresent()) {
            annotatedName = ((RequestParam) optionalAnnotation.get()).name();
        }
        return annotatedName;
    }

}
