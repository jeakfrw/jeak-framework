package de.fearnixx.jeak.service.controller.controller;


import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodParameter {
    private static final Logger logger = LoggerFactory.getLogger(MethodParameter.class);
    private final int position;
    private final Class<?> type;
    private final String name;
    private final List<? extends Annotation> annotations;

    public MethodParameter(int position, Class<?> type, String name, List<? extends Annotation> annotations) {
        this.position = position;
        this.type = type;
        this.name = name;
        this.annotations = annotations;
    }

    public int getPosition() {
        return position;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean hasAnnotation(Class<? extends Annotation> clazz) {
        return annotations.stream()
                .map(Annotation::annotationType)
                .anyMatch(annotationType -> annotationType.equals(clazz));
    }

    /**
     *
     * @param clazz
     * @return
     */
    public Optional<? extends Annotation> getAnnotation(Class<? extends Annotation> clazz) {
        return annotations.stream()
                .filter(o -> o.annotationType().equals(clazz))
                .findFirst();
    }

    public Optional<Object> callAnnotationFunction(Function<Annotation, Object> function, Class<? extends Annotation> annotation) {
        Optional<? extends Annotation> optionalAnnotation = getAnnotation(annotation);
        Object returnValue = null;
        if (optionalAnnotation.isPresent()) {
            returnValue = function.apply(optionalAnnotation.get());
        }
        return Optional.ofNullable(returnValue);
    }
}
