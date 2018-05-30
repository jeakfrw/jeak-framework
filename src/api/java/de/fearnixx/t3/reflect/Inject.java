package de.fearnixx.t3.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a field to be targeted by injections.
 * Value is determined by the field type.
 *
 * Special cases:
 * * {@link Config}
 * * {@link DataSource}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
}
