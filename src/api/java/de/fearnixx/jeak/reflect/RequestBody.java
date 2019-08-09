package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a parameter to be filled by the request body of a call.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestBody {

    /**
     * REQUIRED Specify the class of the used variable.
     */
    Class<?> type();

    /**
     * REQUIRED Specify the name of the used variable.
     */
    String name();
}
