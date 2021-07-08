package de.fearnixx.jeak.reflect.http.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates an endpoint method parameter to be filled with the request body received.
 * This is only applicable to {@link de.fearnixx.jeak.service.http.RequestMethod#POST}, {@link de.fearnixx.jeak.service.http.RequestMethod#PUT}
 * and {@link de.fearnixx.jeak.service.http.RequestMethod#PATCH}
 *
 * @implNote Currently, only two method parameter types are supported! If the type is {@link String}, the serialized content will be used.
 * If the type is of a custom class, Jackson will be used to attempt JSON deserialization of the request body.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestBody {
}
