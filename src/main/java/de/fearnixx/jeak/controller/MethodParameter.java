package de.fearnixx.jeak.controller;

import java.lang.annotation.Annotation;
import java.util.List;

public class MethodParameter {
    private final int position;
    private final Class<?> type;
    private final String name;
    private final List<Class<? extends Annotation>> annotations;

    public MethodParameter(int position, Class<?> type, String name, List<Class<? extends Annotation>> annotations) {
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

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return this.annotations.contains(annotation);
    }
}
