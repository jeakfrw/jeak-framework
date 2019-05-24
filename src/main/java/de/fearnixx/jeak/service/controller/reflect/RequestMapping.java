package de.fearnixx.jeak.service.controller.reflect;

import de.fearnixx.jeak.service.controller.connection.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a specific mapping for the REST controller.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {
    RequestMethod method();
    String endpoint() default "";
    boolean isSecured() default false;
    String contentType() default "application/json";
}
