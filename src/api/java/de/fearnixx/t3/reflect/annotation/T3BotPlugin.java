package de.fearnixx.t3.reflect.annotation;

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
    String id();
    String version() default "0.0.0";
    String[] depends() default {};
    String[] requireAfter() default {};
}
