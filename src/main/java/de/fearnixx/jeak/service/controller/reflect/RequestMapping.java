package de.fearnixx.jeak.service.controller.reflect;

import de.fearnixx.jeak.service.controller.connection.RequestMethod;

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
 * isSecured(): Specify whether or not the calls to this endpoint should use an authorization scheme.
 *
 * contentType(): override the "ContentType"-HTTP header. Its default is "application/json" which is also also the
 * content type to which all the data is converted to.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {
    RequestMethod method();
    String endpoint();
    boolean isSecured() default true;
    String contentType() default "application/json";
}
