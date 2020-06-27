package de.fearnixx.jeak.reflect.http;

import de.fearnixx.jeak.service.http.request.IRequestContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a parameter to be filled from the request context.
 *
 * @apiNote The parameter injection will perform type casting within the bounds of class assignability compatibility.
 * It will also resolve {@link java.util.Optional} but (due to type erasure) cannot ensure the generic contract at runtime unless the optHint parameter is set.
 *
 * @implNote Some usages of this cause side-effects, please see the implementation notes on {@link IRequestContext.Attributes}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestContext {

    /**
     * Denotes the attribute name to be used for the lookup.
     * If none, the parameter injection will attempt to insert the {@link IRequestContext} instance.
     * @see IRequestContext.Attributes for more information on available attributes.
     */
    String attribute() default "";

    /**
     * For parameters of {@link java.util.Optional} type, this param can be specified to re-enable type compatibility checks.
     */
    Class<?> optHint() default Object.class;
}
