package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import de.mlessmann.confort.api.lang.IConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 */
public class LocalizationUnit extends Configurable implements ILocalizationUnit {

    private static final Logger logger = LoggerFactory.getLogger(LocalizationUnit.class);
    private static final String LANG_NODE_NAME = "langs";

    private final String unitId;
    private final IConfig configRef;
    private final ILocalizationService localizationService;
    private final Map<String, LocaleContext> contextCache = new ConcurrentHashMap<>();

    public LocalizationUnit(String unitId, IConfig configRef, ILocalizationService localizationService) {
        super(LocalizationUnit.class);
        this.unitId = unitId;
        this.configRef = configRef;
        this.localizationService = localizationService;
        if (!loadConfig()) {
            throw new IllegalStateException("Failed to load language configuration!");
        }
    }

    @Override
    public String getUnitId() {
        return unitId;
    }

    @Override
    public ILocaleContext getContext(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null!");
        String languageTag = locale.toLanguageTag();

        synchronized (contextCache) {
            LocaleContext cached = contextCache.getOrDefault(languageTag, null);
            if (cached != null) {
                return cached;
            } else {
                IConfigNode langNode = getConfig().getNode(LANG_NODE_NAME, languageTag);

                if (!langNode.isVirtual()) {
                    var localeCtx = new LocaleContext(unitId, locale, langNode);
                    contextCache.put(languageTag, localeCtx);
                    return localeCtx;

                } else if (!localizationService.getFallbackLocale().toLanguageTag().equals(languageTag)) {
                    return getContext(localizationService.getFallbackLocale());

                } else {
                    logger.warn("[{}] Failed to load default language as a fallback for: {}", unitId, languageTag);
                    return new LocaleContext(unitId, locale, getConfig().createNewInstance());
                }
            }
        }
    }

    @Override
    public ILocaleContext getContext(IClient client) {
        Locale locale = localizationService.getLocaleOfClient(client);
        return getContext(locale);
    }

    @Override
    public ILocaleContext getContext(IUser user) {
        Locale locale = localizationService.getLocaleOfUser(user);
        return getContext(locale);
    }

    @Override
    public ILocaleContext getContext(String ts3CountryCode) {
        Objects.requireNonNull(ts3CountryCode, "TS3 country code must not be null!");
        Locale locale = localizationService.getLocaleForCountryId(ts3CountryCode);
        if (locale != null) {
            return getContext(locale);
        }
        return null;
    }

    @Override
    public void loadDefaultsFromResource(ClassLoader classLoader, String resourceURI) {
        Objects.requireNonNull(resourceURI, "Defaults definition resource must not be null!");

        InputStream inputStream = classLoader.getResourceAsStream(resourceURI);
        if (inputStream != null) {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                final URI sourceLocator = URI.create("resource:" + resourceURI);
                logger.debug("[{}] Loading language definition defaults from: {}", unitId, resourceURI);
                loadDefaultsFromStream(reader, sourceLocator);
            } catch (IOException | ParseException e) {
                logger.error("[{}] Failed to load default configuration from resource URI: {}", unitId, resourceURI, e);
            }
        } else {
            logger.error("[{}] Failed to find resource URI: {}", unitId, resourceURI);
        }
    }

    @Override
    public void loadDefaultsFromFile(File file) {
        Objects.requireNonNull(file, "Defaults definition file must not be null!");

        try (FileReader fileReader = new FileReader(file)) {
            logger.debug("[{}] Loading language definition defaults from: {}", unitId, file.getPath());
            loadDefaultsFromStream(fileReader, file.toURI());
        } catch (ParseException | IOException e) {
            logger.error("[{}] Failed to load default configuration from file: {}", unitId, file.getAbsoluteFile().getPath(), e);
        }
    }

    @Override
    public void loadDefaultsFromNode(IConfigNode configNode) {
        Objects.requireNonNull(configNode, "Defaults definition node must not be null!");

        logger.debug("[{}] Attempting to load language definition defaults from config node.", unitId);
        configNode.getNode(LANG_NODE_NAME)
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((defLanguageKey, defLanguageEntries) -> {

                    if (!defLanguageEntries.isMap()) {
                        logger.warn("[{}] Non-map language node found: {}", unitId, defLanguageKey);
                    } else {
                        defLanguageEntries.asMap().forEach((defTemplateId, defTemplate) -> {
                            IConfigNode storedTemplateNode = getConfig().getNode(LANG_NODE_NAME, defLanguageKey, defTemplateId);

                            if (storedTemplateNode.isVirtual()) {
                                String templateStr = defTemplate.optString(null);

                                if (templateStr != null) {
                                    logger.debug("[{}] Loading default template for key: {}.{}", unitId, defLanguageKey, defTemplateId);
                                    storedTemplateNode.setString(templateStr);
                                } else {
                                    logger.warn("[{}] Non-string template node found: {}.{}", unitId, defLanguageKey, defTemplateId);
                                }
                            }
                        });
                    }
                });

        save();
    }

    private void loadDefaultsFromStream(Reader reader, URI locator) throws IOException, ParseException {
        IConfigLoader loader = LoaderFactory.getLoader("application/json");
        loadDefaultsFromNode(loader.parse(reader, locator));
    }

    @Override
    protected IConfig getConfigRef() {
        return configRef;
    }

    @Override
    protected String getDefaultResource() {
        return null;
    }

    @Override
    protected boolean populateDefaultConf(IConfigNode root) {
        root.getNode(LANG_NODE_NAME).setMap();
        return true;
    }

    public void save() {
        logger.debug("[{}] Saving language definitions.", unitId);
        saveConfig();
    }
}
