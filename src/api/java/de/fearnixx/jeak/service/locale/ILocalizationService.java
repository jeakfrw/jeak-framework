package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Locale;

/**
 * The localization services takes care of loading and initializing localization units.
 * It provides access to localization units no matter who initially registered them.
 */
public interface ILocalizationService {

    /**
     * Directs the service to generate locales for a unit with the given ID.
     * If a unit is registered already, that unit is returned without any additional action.
     *
     * Registrations are recommended to be done in {@link IBotStateEvent.IPreInitializeEvent}
     */
    ILocalizationUnit registerUnit(String unitId);

    /**
     * Return the locale representation for the given county code.
     * E.g. de or au would be de_DE and de_AU respectively.
     */
    Locale getLocaleForCountryId(String ts3countryCode);

    /**
     * Returns the locale associated with a specific client.
     * Two values will be taken into account for this:
     * <ul>
     *     <li>The clients custom language setting, if defined.</li>
     *     <li>The clients country flag from TS3.</li>
     * </ul>
     */
    Locale getLocaleOfClient(IClient client);

    /**
     * Sets the custom language setting for the given client.
     * {@code null} to unset.
     *
     * @implNote This will internally use the profile service, once that's added to bleeding.
     */
    void setLocaleForClient(String clientUniqueId, Locale locale);

    /**
     * Returns the configured fallback locale.
     * If unchanged, this will be en_US.
     */
    Locale getFallbackLocale();
}
