package de.fearnixx.jeak.service.locale;

import java.util.Locale;

public interface ILocalizationUnit {

    /**
     * Calls for the unit to register translation configuration for this locale.
     */
    void registerLanguage(Locale locale);

    /**
     * @see #registerLanguage(Locale)
     */
    void registerLanguage(String ts3countyCode);

    /**
     * Returns a context for the given locale.
     * If a context for the locale is not available, the default context will be returned.
     */
    ILocaleContext getContext(Locale locale);

    /**
     * @see #getContext(Locale)
     */
    ILocaleContext getContext(String ts3CountryCode);
}
