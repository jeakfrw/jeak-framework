package de.fearnixx.jeak.service.locale;

import java.util.Map;
import java.util.Optional;

public interface ILocaleContext {

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
