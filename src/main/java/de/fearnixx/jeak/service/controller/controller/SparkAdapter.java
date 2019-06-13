package de.fearnixx.jeak.service.controller.controller;

import de.fearnixx.jeak.reflect.PathParam;
import de.fearnixx.jeak.reflect.RequestBody;
import de.fearnixx.jeak.reflect.RequestMapping;
import de.fearnixx.jeak.reflect.RequestParam;
import de.fearnixx.jeak.service.controller.RequestMethod;
import de.fearnixx.jeak.service.controller.connection.HttpServer;
import de.fearnixx.jeak.service.controller.connection.IConnectionVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;

import java.util.List;

import static spark.Spark.before;
import static spark.Spark.halt;

public class SparkAdapter extends HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(SparkAdapter.class);
    private IConnectionVerifier connectionVerifier;

    public SparkAdapter(IConnectionVerifier connectionVerifier) {
        this.connectionVerifier = connectionVerifier;
    }

    /**
     * Register a provided controller at the Server utilizing Sparks ability to build hierarchical endpoints.
     *
     * @param controllerContainer A {@link ControllerContainer}.
     */
    public void registerController(ControllerContainer controllerContainer) {
        controllerContainer.getControllerMethodList().forEach(controllerMethod -> {
            String path = buildEndpoint(controllerContainer, controllerMethod);
            registerMethod(controllerMethod.getRequestMethod(),
                    path,
                    generateRoute(path, controllerContainer, controllerMethod));
        });
    }
    /**
     * Registers a method to Spark.
     *
     * @param httpMethod The {@link RequestMethod} to map the method to.
     * @param path       The api path as {@link String}for the given method.
     * @param route      The {@link Route} which is supposed to be invoked when a call to the specified
     *                   {@code path} and {@code httpMethod} is made.
     */
    private void registerMethod(RequestMethod httpMethod, String path, Route route) {
        switch (httpMethod) {
            case GET:
                Spark.get(path, route);
                break;
            case PUT:
                Spark.put(path, route);
                break;
            case POST:
                Spark.post(path, route);
                Spark.options(path, (request, response) -> {
                    response.header("Access-Control-Allow-Methods", "POST, OPTIONS");
                    response.header("Access-Control-Allow-Headers", "Content-Type");
                    response.header("Access-Control-Allow-Origin", "*");
                    return "";
                });
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
            default:
                logger.warn("Failed to register the route to " + path);
                break;
        }
    }

    /**
     * Generate a Spark specific {@link Route} for the provided controller and method.
     *
     * @param path The path for the specified route
     * @param controllerContainer The {@link ControllerContainer} of the controller.
     * @param controllerMethod    The {@link ControllerMethod} of the method.
     * @return A {@link Route} containing the actions of the {@link ControllerMethod}.
     */
    private Route generateRoute(String path, ControllerContainer controllerContainer, ControllerMethod controllerMethod) {
        List<MethodParameter> methodParameterList = controllerMethod.getMethodParameters();
        before(path, (request, response) -> {
            if (controllerMethod.getAnnotation(RequestMapping.class).isSecured()) {
                boolean isAuthorized = connectionVerifier.verifyRequest(path, request.headers("Authorization"));
                if (!isAuthorized) {
                    halt(401);
                }
            }
        });
        return (request, response) -> {
            Object[] methodParameters = new Object[methodParameterList.size()];
            for (MethodParameter methodParameter : methodParameterList) {
                Object retrievedParameter = null;
                if (methodParameter.hasAnnotation(PathParam.class)) {
                    retrievedParameter = transformRequestOption(request.params(getPathParamName(methodParameter)), request, methodParameter);
                } else if (methodParameter.hasAnnotation(RequestParam.class)) {
                    retrievedParameter = transformRequestOption(request.queryMap(getRequestParamName(methodParameter)).value(), request, methodParameter);
                } else if (methodParameter.hasAnnotation(RequestBody.class)) {
                    retrievedParameter = transformRequestOption(request.body(), request, methodParameter);
                }
                methodParameters[methodParameter.getPosition()] = retrievedParameter;
            }
            Object returnValue = controllerContainer.invoke(controllerMethod, methodParameters);
            String contentType = controllerMethod.getAnnotation(RequestMapping.class).contentType();
            if (contentType.contains("json")) {
                response.type(contentType);
                returnValue = toJson(returnValue);
            }
            response.header("Access-Control-Allow-Origin", "*");
            return returnValue;
        };
    }
}
