package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a datasource.
 *
 * Supported field types:
 * * {@link javax.persistence.EntityManager}
 * * {@link Boolean} // Will return whether or not this DataSource is available
 * @deprecated Use {@link javax.persistence.PersistenceUnit} instead. (Also avoids naming collision with JPA.)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
public @interface DataSource {

    /**
     * The DataSource identifier (has to correspond with the configuration key)
     */
    String value();
}
