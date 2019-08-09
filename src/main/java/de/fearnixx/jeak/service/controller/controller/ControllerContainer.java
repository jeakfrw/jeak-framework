package de.fearnixx.jeak.service.controller.controller;

import de.fearnixx.jeak.reflect.RequestMapping;
import de.fearnixx.jeak.reflect.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ControllerContainer {

    private static final Logger logger = LoggerFactory.getLogger(ControllerContainer.class);

    private Object controllerObject;
    private String controllerEndpoint;
    private List<ControllerMethod> controllerMethodList;

    public ControllerContainer(Object o) {
        Objects.requireNonNull(o);
        initControllerContainer(o);
    }

    /**
     * Initialize the attributes of the controller container.
     *
     * @param o The controler {@link Object}.
     */
    private void initControllerContainer(Object o) {
        logger.debug("Searching for endpoints on controller: {}", o.getClass());
        this.controllerObject = o;
        this.controllerEndpoint = extractControllerRoute(o);
        this.controllerMethodList = Arrays.stream(o.getClass().getMethods())
                .filter(method -> method.getAnnotation(RequestMapping.class) != null)
                .map(method -> {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    logger.debug("Found endpoint: {}: {} at: {}", mapping.method(), mapping.endpoint(), method.getName());
                    return new ControllerMethod(method, mapping.method(), mapping.endpoint());
                })
                .collect(Collectors.toList());
    }

    private String extractControllerRoute(Object o) {
        RestController annotation = o.getClass().getAnnotation(RestController.class);
        return annotation.pluginId().concat(annotation.endpoint());
    }

    /**
     * Invoke one of the controllers {@link ControllerMethod}.
     */
    public Object invoke(ControllerMethod controllerMethod, Object... methodParameters)
            throws InvocationTargetException, IllegalAccessException {
        return controllerMethod.invoke(controllerObject, methodParameters);
    }

    public Object getControllerObject() {
        return controllerObject;
    }

    public String getControllerEndpoint() {
        return controllerEndpoint;
    }

    public List<ControllerMethod> getControllerMethodList() {
        return controllerMethodList;
    }

    public Class<?> getControllerClass() {
        return controllerObject.getClass();
    }
}
