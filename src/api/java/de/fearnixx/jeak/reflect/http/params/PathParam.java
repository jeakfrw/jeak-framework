package de.fearnixx.jeak.reflect.http.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an endpoint method parameter to be derived from the requests path pattern.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathParam {

    /**
     * If the parameter name from the path pattern differs from the method parameter name, this should be the
     * name used in the path pattern.
     */
    String name() default "";
}