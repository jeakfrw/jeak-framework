package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.event.bot.IBotStateEvent;

import java.util.Locale;

public interface ILocalizationService {

    /**
     * Directs the service to generate locales for a unit with the given ID.
     * If a unit is registered already, that unit is returned without any additional action.
     *
     * Registrations are recommended to be done in {@link IBotStateEvent.IPreInitializeEvent}
     */
    ILocaleContext registerUnit(String unitId);

    /**
     * Return the locale representation for the given county code.
     * E.g. de or au would be de_DE and de_AU respectively.
     */
    Locale getLocaleForCountryId(String ts3countryCode);
}
