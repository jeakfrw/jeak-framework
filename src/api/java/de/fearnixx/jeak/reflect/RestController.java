package de.fearnixx.jeak.reflect;

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
     * Specify the id of the used plugin.
     * This is independent of the pluginId specified in {@link de.fearnixx.jeak.reflect.JeakBotPlugin}.
     */
    String pluginId();

    /**
     * REQUIRED if you use more than one controller. Specify the endpoint of the controller.
     */
    String endpoint();
}
