package de.fearnixx.jeak.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

public abstract class AbstractSpecialProvider<T extends Annotation> {

    public abstract Class<T> getAnnotationClass();

    public boolean test(Field f) {
        return f.getAnnotation(getAnnotationClass()) != null;
    }

    public abstract Optional<Object> provideWith(InjectionContext ctx, Field f);
}
