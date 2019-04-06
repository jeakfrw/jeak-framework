package de.fearnixx.jeak.service.locale;

import de.mlessmann.confort.api.IConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the locale context.
 * {@inheritDoc}
 */
public class LocalizationContext implements ILocaleContext {

    private static final Logger logger = LoggerFactory.getLogger(LocalizationContext.class);

    private final IConfigNode languageNode;
    private final Map<String, MessageRep> preExploded = new HashMap<>();

    public LocalizationContext(IConfigNode languageNode) {
        this.languageNode = languageNode;
        preExplodeMessages();
    }

    private void preExplodeMessages() {
        languageNode.optMap()
                .orElseThrow(() -> new IllegalStateException("Language context nodes may only be maps!"))
                .forEach((msgId, template) -> {
                    String templateString = template.optString()
                            .orElseThrow(() -> new IllegalStateException("Message templates may only be strings!"));
                    preExploded.put(msgId, new MessageRep(templateString));
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> optMessage(String messageId, Map<String, String> messageParams) {
        return Optional.ofNullable(uncheckedGetMessage(messageId, messageParams));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> optMessage(String messageId) {
        return Optional.ofNullable(uncheckedGetMessage(messageId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uncheckedGetMessage(String messageId, Map<String, String> messageParams) {
        MessageRep message = preExploded.getOrDefault(messageId, null);

        if (message != null) {
            try {
                message.getWithParams(messageParams);
            } catch (MissingParameterException e) {
                logger.error("Cannot build message \"{}\" with missing parameter: {}", messageId, e.getParameterName());
                return message.getRawTemplate();
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uncheckedGetMessage(String messageId) {
        MessageRep message = preExploded.getOrDefault(messageId, null);
        return message != null ? message.getRawTemplate() : null;
    }
}
