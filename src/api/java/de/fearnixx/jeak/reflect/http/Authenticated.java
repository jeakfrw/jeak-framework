package de.fearnixx.jeak.reflect.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a method annotated with {@link RequestMapping} as requiring authentication.
 * To facilitate basic authorization checks, a permission can be set as required for the endpoint.
 *
 * @since 1.2.0 (experimental)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Authenticated {

    /**
     * The permissions required to access the endpoint at all.
     * The requesting party must have all the given permissions in order to access the endpoint (AND).
     */
    String[] permissions() default {};
}
