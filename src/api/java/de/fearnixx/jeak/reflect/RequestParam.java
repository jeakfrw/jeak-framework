package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a parameter to be filled by a query parameter from a call.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {

    /**
     * REQUIRED if its something else than a {@link String} Specify the type of the expected variable.
     */
    Class<?> type() default String.class;

    /**
     * REQUIRED Specify the name of the expected variable.
     */
    String name();
}
