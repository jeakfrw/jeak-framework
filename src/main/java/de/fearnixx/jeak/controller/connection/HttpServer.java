package de.fearnixx.jeak.controller.connection;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import spark.Route;
import spark.Spark;

import static spark.Spark.port;

/**
 * A wrapper for the http server.
 */
public class HttpServer {
    private int port = 8723;

    public HttpServer() {
    }

    public HttpServer(int port) {
        this.port = port;
    }

    public void init() {
        port(port);
    }

    public void registerMethod(RequestMethod httpMethod, String path, Method method) {
        Route route = generateRoute(method);
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

    private Route generateRoute(Method method) {
        return (request, response) -> {
            Parameter[] parameters = method.getParameters();
            Object[] methodParams = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                methodParams[i] = request.attribute(parameters[i].getName());
            }
            return method.invoke(methodParams);
        };
    }
}
