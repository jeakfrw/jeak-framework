package de.fearnixx.jeak.service.controller.controller;

import de.fearnixx.jeak.service.controller.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ControllerMethod {
    private final Method method;
    private final RequestMethod requestMethod;
    private final String path;

    public ControllerMethod(Method method, RequestMethod requestMethod, String path) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(requestMethod);
        Objects.requireNonNull(path);
        this.method = method;
        this.requestMethod = requestMethod;
        this.path = path;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    /**
     * Invoke the method from its {@link ControllerContainer}.
     *
     * @param controller
     * @param methodParameters
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(Object controller, Object... methodParameters) throws InvocationTargetException, IllegalAccessException {
        return this.method.invoke(controller, methodParameters);
    }

    /**
     * Extract the method parameters from a method.
     *
     * @return A List of {@link MethodParameter}
     */
    public List<MethodParameter> getMethodParameters() {
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

    /**
     *
     * @param annotationClass
     * @param <T>
     * @return
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }
}
