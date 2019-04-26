package de.fearnixx.jeak.controller.connection;

import spark.Route;
import spark.Spark;

import static spark.Spark.port;

/**
 * A wrapper for the http server.
 */
public class HttpServer {
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

}
