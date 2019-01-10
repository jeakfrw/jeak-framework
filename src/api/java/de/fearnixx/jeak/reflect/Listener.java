package de.fearnixx.jeak.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for use with EventListeners.
 * Listener methods are searched using this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listener {

    /**
     * Allows listeners to define an order based on which the listener will be prioritized.
     * Highest is run latest.
     */
    short order() default 0;

    abstract class Orders {

        public static final short LATEST = Short.MAX_VALUE;
        public static final short LATE = Short.MAX_VALUE / 2;
        public static final short LATER = Short.MAX_VALUE / 4;

        public static final short NORMAL = (short) 0;

        public static final short EARLIER = Short.MIN_VALUE / 4;
        public static final short EARLY = Short.MIN_VALUE / 2;
        public static final short SYSTEM = Short.MIN_VALUE;
    }
}
