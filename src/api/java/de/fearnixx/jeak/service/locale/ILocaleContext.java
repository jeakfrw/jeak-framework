package de.fearnixx.jeak.service.locale;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Locale contexts represent localization units focused on one specific locale.
 * The actual translated text templates are located in their respective locale context.
 *
 * Locale contexts are constructed within {@link ILocalizationUnit}s and always belong to their unit.
 */
public interface ILocaleContext {

    /**
     * ID of the parent localization unit this context is associated to.
     */
    String getUnitId();

    /**
     * The locale this context is associated to.
     */
    Locale getLocale();

    /**
     * Returns the registered message with its parameters replaced by the values stored in the given params.
     * @throws IllegalStateException when the message is not present.
     */
    String getMessage(String messageId, Map<String, String> messageParams);

    /**
     * Returns the registered message with its parameters untouched.
     * @throws IllegalStateException when the message is not present.
     */
    String getMessage(String messageId);

    /**
     * Returns the registered message with its parameters replaced by the values stored in the given params.
     * Returns {@link Optional#empty()} for unregistered messages.
     */
    Optional<String> optMessage(String messageId, Map<String, String> messageParams);

    /**
     * Returns the registered message with its parameters untouched.
     * Returns {@link Optional#empty()} for unregistered messages.
     */
    Optional<String> optMessage(String messageId);

    /**
     * Returns the registered message with its parameters replaced by the values stored in the given params.
     * Returns {@code null} for unregistered messages.
     */
    String uncheckedGetMessage(String messageId, Map<String, String> messageParams);

    /**
     * Returns the registered message with its parameters untouched.
     * Returns {@code null} for unregistered messages.
     */
    String uncheckedGetMessage(String messageId);
}
