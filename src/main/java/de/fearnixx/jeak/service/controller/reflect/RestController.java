package de.fearnixx.jeak.service.controller.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a REST controller and specifies the specific REST endpoint of the class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {
    String endpoint() default "";
    String pluginId() default "default";
}
