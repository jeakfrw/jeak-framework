package de.fearnixx.jeak.reflect.http;

import de.fearnixx.jeak.service.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as method to be available via the controller.
 *
 * method(): REQUIRED Specify the used HTTP-method.
 *
 * endpoint(): REQUIRED Specify the endpoint for the annotated method.
 *
 * isSecured(): Specify whether the calls to this endpoint should use an authorization scheme.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {
    RequestMethod method();
    String endpoint();
    boolean isSecured() default true;
}
