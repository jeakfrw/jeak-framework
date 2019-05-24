package de.fearnixx.jeak.service.controller.controller;

import de.fearnixx.jeak.service.controller.reflect.RequestMapping;
import de.fearnixx.jeak.service.controller.reflect.RestController;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ControllerContainer {
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
        this.controllerObject = o;
        this.controllerEndpoint = extractControllerRoute(o);
        this.controllerMethodList = Arrays.stream(o.getClass().getDeclaredMethods())
                .map(method -> new ControllerMethod(
                        method,
                        method.getAnnotation(RequestMapping.class).method(),
                        method.getAnnotation(RequestMapping.class).endpoint()))
                .collect(Collectors.toList());
    }

    private String extractControllerRoute(Object o) {
        return o.getClass().getAnnotation(RestController.class).endpoint();
    }

    /**
     * Invoke one of the controllers {@link ControllerMethod}.
     *
     * @param controllerMethod
     * @param methodParameters
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(ControllerMethod controllerMethod, Object... methodParameters) throws InvocationTargetException, IllegalAccessException {
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

}
