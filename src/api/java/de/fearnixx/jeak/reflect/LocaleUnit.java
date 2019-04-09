package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.locale.ILocalizationUnit;

/**
 * Indicates that a locale unit should be injected.
 */
public @interface LocaleUnit {

    /**
     * The id for the localization unit.
     * If empty, the current injection context ID will be used.
     */
    String value() default "";

    /**
     * Implicitly calls {@link ILocalizationUnit#loadDefaultsFromResource(String)} after unit construction.
     * This is a convenience value so plugins that store their default language files in the classpath.
     *
     * @apiNote For performance and consistency reasons, it is recommended to use this value at most <strong>once per unit!</strong>
     *
     * @see ILocalizationUnit#loadDefaultsFromResource(String) for more information.
     */
    String defaultResource() default "";
}
