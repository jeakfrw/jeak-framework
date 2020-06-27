package de.fearnixx.jeak.reflect.http;

import de.fearnixx.jeak.service.http.request.IRequestContext;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a parameter to be filled from the request context.
 *
 * @apiNote The parameter injection will work within the bounds of class assignability compatibility.
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
    @Nonnull
    String attribute() default "";

    /**
     * Whether or not this parameter has to be set.
     * Unset, non-required values are passed as {@code null}.
     */
    boolean required() default true;
}
