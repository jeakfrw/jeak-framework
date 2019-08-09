package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.controller.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as method to be available via the controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {

    /**
     * REQUIRED Specify the used HTTP-method.
     * @implNote Please note that {@code OPTIONS} is specially covered by the REST service.
     */
    RequestMethod method();

    /**
     * REQUIRED Specify the endpoint for the annotated method. This will be concatenated with {@code /api/<plugin-id>/}.
     */
    String endpoint();

    /**
     * Specify whether or not the calls to this endpoint should use an authorization scheme.
     */
    boolean isSecured() default true;
}
