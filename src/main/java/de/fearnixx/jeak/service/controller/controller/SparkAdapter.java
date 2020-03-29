package de.fearnixx.jeak.service.controller.controller;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.controller.RequestMethod;
import de.fearnixx.jeak.service.controller.ResponseEntity;
import de.fearnixx.jeak.service.controller.connection.HttpServer;
import de.fearnixx.jeak.service.controller.connection.IConnectionVerifier;
import de.fearnixx.jeak.service.controller.connection.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class SparkAdapter extends HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(SparkAdapter.class);
    public static final int NUM_THREADS = Main.getProperty("jeak.http.poolsize", -1);
    public static final int MAX_THREADS = Main.getProperty("jeak.sparkadapter.maxpoolsize", 8);
    public static final int MIN_THREADS = Main.getProperty("jeak.sparkadapter.minpoolsize", 3);
    public static final int TIMEOUT_MILLIS = 30000;
    private IConnectionVerifier connectionVerifier;
    private Map<String, String> headers;
    private Service service;

    public SparkAdapter(IConnectionVerifier connectionVerifier, RestConfiguration restConfiguration) {
        super(restConfiguration);
        service = Service.ignite();
        // only use NUM_THREADS, if it was configured
        if (NUM_THREADS > 0) {
            service.threadPool(NUM_THREADS, NUM_THREADS, TIMEOUT_MILLIS);
        } else {
            service.threadPool(MAX_THREADS, MIN_THREADS, TIMEOUT_MILLIS);
        }
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
        checkAndSetCors(path);
        switch (httpMethod) {
            case GET:
                service.get(path, route);
                break;
            case PUT:
                service.put(path, route);
                break;
            case POST:
                service.post(path, route);
                break;
            case PATCH:
                service.patch(path, route);
                break;
            case DELETE:
                service.delete(path, route);
                break;
            case HEAD:
                service.head(path, route);
                break;
            default:
                logger.warn("Failed to register the route for: {}", path);
                break;
        }
    }

    private void checkAndSetCors(String path) {
        if (isCorsEnabled()) {
            service.options(path, (request, response) -> {
                headers = setHeaders(response, new HashMap<>());
                return "";
            });
        }
    }

    /**
     * Generate a Spark specific {@link Route} for the provided controller and method.
     *
     * @param path                The path for the specified route
     * @param controllerContainer The {@link ControllerContainer} of the controller.
     * @param controllerMethod    The {@link ControllerMethod} of the method.
     * @return A {@link Route} containing the actions of the {@link ControllerMethod}.
     */
    private Route generateRoute(String path, ControllerContainer controllerContainer, ControllerMethod controllerMethod) {
        List<MethodParameter> methodParameterList = controllerMethod.getMethodParameters();
        addBeforeHandlingCheck(path, controllerContainer, controllerMethod);
        return (request, response) -> {
            Object[] methodParameters = extractParameters(methodParameterList, request);
            Object returnValue = controllerContainer.invoke(controllerMethod, methodParameters);
            Map<String, String> additionalHeaders = new HashMap<>();
            if (returnValue instanceof ResponseEntity) {
                ResponseEntity responseEntity = (ResponseEntity) returnValue;
                additionalHeaders.putAll(responseEntity.getHeaders());
                returnValue = responseEntity.getResponseEntity();
            }
            headers = setHeaders(response, additionalHeaders);

            String contentType = headers.get("Content-Type");
            if (contentType == null || ("application/json".equals(contentType))) {
                response.type("application/json");
                returnValue = toJson(returnValue);
            }
            return returnValue;
        };
    }

    private Object[] extractParameters(List<MethodParameter> methodParameterList, Request request) {
        Object[] methodParameters;
        if (methodParameterList == null) {
            methodParameters = new Object[0];
        } else {
            methodParameters = new Object[methodParameterList.size()];
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
        }
        return methodParameters;
    }

    private void addBeforeHandlingCheck(String path, ControllerContainer controllerContainer, ControllerMethod controllerMethod) {
        service.before(path, (request, response) -> {
            controllerContainer.getAnnotation(RestController.class).ifPresent(restController -> {
                if (getRestConfiguration().rejectUnencryptedTraffic().orElse(RestConfiguration.DEFAULT_HTTPS_REJECT_UNENCRYPTED) && !isProtocolHttps(request)) {
                    logger.debug("HTTPS enforcement enabled, non HTTPS request for {} blocked", path);
                    service.halt(426, "{\"errors\": [\"Use of HTTPS is mandatory for this endpoint\"]}");
                }
            });
            controllerMethod.getAnnotation(RequestMapping.class).ifPresent(requestMapping -> {
                if (requestMapping.isSecured()) {
                    boolean isAuthorized = connectionVerifier.verifyRequest(path, request.headers("Authorization"));
                    if (!isAuthorized) {
                        logger.debug("Authorization for Request to {} failed", path);
                        service.halt(401);
                    }
                }
            });
        });
    }

    private boolean isProtocolHttps(Request request) {
        return request.protocol().contains(HTTPS_PROTOCOL) ||
                (getRestConfiguration().isBehindSslProxy().orElse(false)
                        && request.headers(X_FORWARDED_PROTO_HEADER) != null &&
                        !request.headers(X_FORWARDED_PROTO_HEADER).isBlank() &&
                        request.headers(X_FORWARDED_PROTO_HEADER).contains(HTTPS_PROTOCOL));
    }

    @Override
    public void start() {
        getRestConfiguration().getPort().ifPresent(service::port);
        getRestConfiguration().isHttpsEnabled().ifPresent(isHttpsEnabled -> {
            if (Boolean.TRUE.equals(isHttpsEnabled)) {
                logger.info("Https enabled");
                initHttps();
            } else {
                logger.info("HTTPS disabled");
            }
        });

    }

    private void initHttps() {
        Optional<String> httpsKeystorePath = getRestConfiguration().getHttpsKeystorePath();
        Optional<String> httpsKeystorePassword = getRestConfiguration().getHttpsKeystorePassword();
        Optional<String> httpsTruststorePath = getRestConfiguration().getHttpsTruststorePath();
        Optional<String> httpsTruststorePassword = getRestConfiguration().getHttpsTruststorePassword();

        if (httpsKeystorePath.isPresent() && httpsKeystorePassword.isPresent()) {
            service.secure(httpsKeystorePath.get(), httpsKeystorePassword.get(), null, null);
            if (httpsTruststorePath.isPresent() && httpsTruststorePassword.isPresent()) {
                service.secure(httpsKeystorePath.get(), httpsKeystorePassword.get(), httpsTruststorePath.get(), httpsTruststorePassword.get());
            }
        }
    }

    /**
     * This method adds the provided {@link Map} of headers to the provided {@link Response}.
     *
     * @param response
     * @param additionalHeaders
     * @return
     */
    private Map<String, String> setHeaders(Response response, Map<String, String> additionalHeaders) {
        Map<String, String> headerMap = loadHeaders();
        if (isCorsEnabled()) {
            headerMap.putAll(loadCorsHeaders());
        }
        // Important to add the additional headerMap afterwards, so they can override the others
        headerMap.putAll(additionalHeaders);
        headerMap.forEach(response::header);
        return headerMap;
    }
}
