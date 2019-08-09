package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a class to be loaded as a plugin by the framework.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JeakBotPlugin {
    /**
     * The plugin ID
     * Must match "^[a-zA-Z.][a-zA-Z0-9.]+$".
     * Plugin IDs should always be handled case-insensitive where user input is processed!
     */
    String id();

    /**
     * The plugin version will be used to determine improperly satisfied dependencies.
     * The framework assumes semver to be used for version identifiers: https://semver.org/
     */
    String version() default "0.0.0";

    /**
     * Hard dependencies
     * The framework will refuse to start if unresolved and when circularity is detected! You will need to avoid that.
     */
    String[] depends() default {};

    /**
     * Bot version revisions!
     * Bot will refuse to start with plugins that won't work.
     * Bot will issue a warning if builtAgainst does not match with its version
     *
     * <p>Empty string is a wildcard!
     * This means no breaking version and built against ANY.
     *
     * @apiNote Currently unused. Will be added later
     */
    String builtAgainst() default "";
}
