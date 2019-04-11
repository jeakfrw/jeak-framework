package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.mail.ITransportUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom injection marker for injecting {@link ITransportUnit}s into objects.
 * See the interface for more information.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransportUnit {

    String name() default "default";
}
