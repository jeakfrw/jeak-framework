package de.fearnixx.jeak.service.locale;

import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import de.mlessmann.confort.api.lang.IConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * {@inheritDoc}
 */
public class LocalizationUnit implements ILocalizationUnit {

    private static final Logger logger = LoggerFactory.getLogger(LocalizationUnit.class);

    private final String unitId;
    private final IConfigNode configuration;
    private final Function<String, Locale> localeResolver;

    public LocalizationUnit(String unitId, IConfigNode configuration, Function<String, Locale> localeResolver) {
        this.unitId = unitId;
        this.configuration = configuration;
        this.localeResolver = localeResolver;
    }

    @Override
    public String getUnitId() {
        return unitId;
    }

    @Override
    public ILocaleContext getContext(Locale locale) {
        Objects.requireNonNull(locale, "Locale may not be null!");

        String languageTag = locale.toLanguageTag();
        IConfigNode langNode = configuration.getNode("langs", languageTag);

        if (!langNode.isVirtual()) {
            return new LocaleContext(unitId, locale, langNode);

        } else if (!getDefaultLanguage().equals(languageTag)) {
            return getContext(getDefaultLanguage());
        } else {
            logger.warn("[{}] Failed to load default language as a fallback for: {}", unitId, languageTag);
            return new LocaleContext(unitId, locale, configuration.createNewInstance());
        }
    }

    private String getDefaultLanguage() {
        return configuration.getNode("defaultLanguage").optString("en_US");
    }

    @Override
    public ILocaleContext getContext(String ts3CountryCode) {
        Objects.requireNonNull(ts3CountryCode, "TS3 country code may not be null!");
        Locale locale = localeResolver.apply(ts3CountryCode);
        if (locale != null) {
            getContext(locale);
        }
        return null;
    }

    @Override
    public void loadDefaultsFromResource(String resourceURI) {
        InputStream inputStream = LocalizationUnit.class.getClassLoader().getResourceAsStream(resourceURI);

        if (inputStream != null) {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                loadDefaultsFromStream(reader);
            } catch (IOException | ParseException e) {
                logger.error("[{}] Failed to load default configuration from resource URI: {}", unitId, resourceURI, e);
            }
        }
    }

    @Override
    public void loadDefaultsFromFile(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            loadDefaultsFromStream(fileReader);
        } catch (ParseException | IOException e) {
            logger.error("[{}] Failed to load default configuration from file: {}", unitId, file.getAbsoluteFile().getPath(), e);
        }
    }

    @Override
    public void loadDefaultsFromNode(IConfigNode configNode) {
        configNode.getNode("defaultLanguage")
                .optString()
                .ifPresent(str -> {
                    IConfigNode defaultLanguageNode = configuration.getNode("defaultLanguage");
                    if (defaultLanguageNode.isVirtual()) {
                        defaultLanguageNode.setString(str);
                    }
                });

        configNode.getNode("langs")
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((languageKey, value) -> {
                    if (!value.isMap()) {
                        logger.warn("[{}] Non-map language node found: {}", unitId, languageKey);
                    } else {
                        value.asMap().forEach((templateId, template) -> {
                            IConfigNode templateNode = configuration.getNode(languageKey, templateId);

                            if (templateNode.isVirtual()) {
                                String templateStr = template.optString(null);

                                if (templateStr != null) {
                                    template.setString(templateStr);
                                } else {
                                    logger.warn("[{}] Non-string template node found: {}.{}", unitId, languageKey, templateId);
                                }
                            }
                        });
                    }
                });
    }

    private void loadDefaultsFromStream(Reader reader) throws IOException, ParseException {
        IConfigLoader loader = LoaderFactory.getLoader("application/json");
        loadDefaultsFromNode(loader.parse(reader));
    }
}
