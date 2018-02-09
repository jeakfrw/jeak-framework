package de.fearnixx.t3.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Life4YourGames on 22.05.17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface T3BotPlugin {
    /**
     * The plugin ID
     * Must match "^[a-z.]+$"
     */
    String id();

    /**
     * The plugin version will be used to determine improperly satisfied dependencies
     */
    String version() default "0.0.0";

    /**
     * Hard dependencies
     * Bot will refuse to start if unresolved
     * Bot will refuse to start if circular! You will need to avoid that.
     */
    String[] depends() default {};

    /**
     * Soft dependencies - Bot will try to load this plugin AFTER all of these
     * This cannot be 100% guaranteed though.
     */
    String[] requireAfter() default {};

    /**
     * T3Bot version revisions!
     * Bot will refuse to start with plugins that won't work.
     * Bot will issue a warning if builtAgainst does not match with its version
     *
     * Empty string is a wildcard!
     * This means no breaking version and built against ANY.
     *
     * - Note: Currently unused. Will be added later
     * TODO: Add this *derp*
     */
    String builtAgainst() default "";
    String breaksBefore() default "";
    String breaksAfter() default "";
}
