package de.fearnixx.jeak.reflect;

import de.mlessmann.confort.api.IConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a field to be filled with configuration representation.
 *
 * <p>Allowed types:
 * * {@link java.nio.file.Path}
 * * {@link java.io.File}
 * * {@link IConfig}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {

    /**
     * Configuration Id - Will determine the file name.
     * If not set the following will be used:
     * * Plugin Id (if available)
     * * ClassName + fieldName (with "config" or "loader" stripped)
     */
    String id() default "";

    /**
     * Will determine what sub directory to use if you happen to have many configuration files.
     */
    String category() default "";

    /**
     * The configuration format.
     * (Currently not in use!)
     */
    String format() default "";
}
