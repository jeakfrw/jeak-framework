package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a parameter to be filled by a request parameter from a call.
 *
 * type(): REQUIRED if its something else than a {@link String} Specify the type of the expected variable.
 *
 * name(): REQUIRED Specify the name of the expected variable.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    Class<?> type() default String.class;
    String name();
}
