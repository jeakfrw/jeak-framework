package de.fearnixx.jeak.reflect.http.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a methods parameter to be filled by a request parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {

    /**
     * If the parameter name differs from the HTTP-contract, this should be the name used in the contract.
     */
    String name() default "";
}
