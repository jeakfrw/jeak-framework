package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a locale unit should be injected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LocaleUnit {

    /**
     * The id for the localization unit.
     * If empty, the current injection context ID will be used.
     */
    String value() default "";

    /**
     * Implicitly calls {@link ILocalizationUnit#loadDefaultsFromResource(ClassLoader, String)} after unit construction.
     * This is a convenience value so plugins that store their default language files in the classpath.
     *
     * @apiNote For performance and consistency reasons,
     * it is recommended to use this value at most <strong>once per unit!</strong>
     *
     * @see ILocalizationUnit#loadDefaultsFromResource(ClassLoader, String) for more information.
     */
    String defaultResource() default "";
}
