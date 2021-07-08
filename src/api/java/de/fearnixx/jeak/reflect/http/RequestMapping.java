package de.fearnixx.jeak.reflect.http;

import de.fearnixx.jeak.service.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a method as being a receiver for HTTP-requests.
 *
 * @since 1.2.0 (experimental)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {

    /**
     * The HTTP method associated with this endpoint.
     */
    RequestMethod method() default RequestMethod.GET;

    /**
     * URI appendix for this endpoint.
     */
    String endpoint();
}
