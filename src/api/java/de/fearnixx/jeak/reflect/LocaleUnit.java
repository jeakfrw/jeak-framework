package de.fearnixx.jeak.reflect;

/**
 * Indicates that a locale unit should be injected.
 */
public @interface LocaleUnit {

    /**
     * The id for the localization unit.
     * If empty, the current injection context ID will be used.
     */
    String value() default "";
}
