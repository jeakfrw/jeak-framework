package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a field to be targeted by injections.
 * Value is determined by the field type.
 *
 * <p>Special cases:
 * * {@link Config}
 * * {@link de.fearnixx.jeak.service.database.IPersistenceUnit}
 * * {@link de.fearnixx.jeak.service.locale.ILocalizationUnit}
 * * {@link de.fearnixx.jeak.service.mail.ITransportUnit}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
}
