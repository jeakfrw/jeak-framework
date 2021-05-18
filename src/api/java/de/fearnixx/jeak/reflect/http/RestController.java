package de.fearnixx.jeak.reflect.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a REST controller. One plugin can have multiple controllers, so the controller determines
 * to which plugin it belongs by using the pluginId.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {

    /**
     * Your plugin id, since controllers are grouped by plugin ID to avoid path collisions between plugins.
     */
    String pluginId();

    /**
     * An endpoint path prefix for all request mappings within this class.
     */
    String path();
}
