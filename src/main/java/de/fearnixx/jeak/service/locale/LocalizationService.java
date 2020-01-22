package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.IServiceManager;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherRegistryService;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.config.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkService(serviceInterface = ILocalizationService.class)
public class LocalizationService extends Configurable implements ILocalizationService {

    private static final String DEFAULT_CONFIG = "/localization/defaultConfig.json";

    private static final Logger logger = LoggerFactory.getLogger(LocalizationService.class);

    @Inject
    private IBot bot;

    @Inject
    @Config(id = "localization")
    private IConfig localizationConfig;

    @Inject
    private IInjectionService injectionService;

    @Inject
    private IServiceManager serviceManager;
    private ICommandService commandService;

    private final Map<String, LocalizationUnit> registeredUnits = new ConcurrentHashMap<>();

    public LocalizationService() {
        super(LocalizationService.class);
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onPreInitialize(IBotStateEvent.IPluginsLoaded event) {
        commandService = serviceManager.provideUnchecked(ICommandService.class);
        LocaleMatcher localeMatcher = injectionService.injectInto(new LocaleMatcher());
        IMatcherRegistryService matcherRegistry = serviceManager.provideUnchecked(IMatcherRegistryService.class);
        matcherRegistry.registerMatcher(localeMatcher);
        File langConfigDir = new File(bot.getConfigDirectory(), "lang");

        if (!langConfigDir.exists() || !langConfigDir.isDirectory()) {
            logger.debug("Creating localization directory.");

            if (!langConfigDir.mkdirs()) {
                logger.error("Failed to create localization directory! Expect errors!");
            }
        }
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        LocaleCommand localeCommand = injectionService.injectInto(new LocaleCommand());
        commandService.registerCommand(localeCommand.getCommandSpec());
        commandService.registerCommand(localeCommand.getArgCommandSpec());
    }

    @Listener
    public void onShutdown(IBotStateEvent.IPreShutdown event) {
        registeredUnits.values().forEach(LocalizationUnit::save);
        saveConfig();
    }

    @Listener
    public void initialize(IBotStateEvent.IInitializeEvent event) {
        if (!loadConfig()) {
            event.cancel();
        }
    }

    @Override
    public ILocalizationUnit registerUnit(String unitId) {
        Objects.requireNonNull(unitId, "Unit identifier must not be null!");

        synchronized (registeredUnits) {
            if (!registeredUnits.containsKey(unitId)) {
                logger.debug("Creating localization unit: {}", unitId);
                File langConfigDir = new File(bot.getConfigDirectory(), "lang");
                File langFile = new File(langConfigDir, unitId + ".json");
                FileConfig langFileRef = new FileConfig(LoaderFactory.getLoader("application/json"), langFile);

                LocalizationUnit unit = new LocalizationUnit(unitId, langFileRef, this);
                registeredUnits.put(unitId, unit);
                return unit;
            }

            return registeredUnits.get(unitId);
        }
    }

    @Override
    public Locale getLocaleForCountryId(String ts3countryCode) {
        Objects.requireNonNull(ts3countryCode, "Country code must not be null!");

        return Locale.forLanguageTag(ts3countryCode);
    }

    @Override
    public Locale getLocaleOfClient(IClient client) {
        IConfigNode customLangNode = getConfig().getNode("customLangs", client.getClientUniqueID());

        if (!customLangNode.isVirtual()) {
            return getLocaleForCountryId(customLangNode.asString());

        } else {
            return client.getProperty(PropertyKeys.Client.COUNTRY)
                    .map(this::getLocaleForCountryId)
                    .orElseGet(this::getFallbackLocale);
        }
    }

    @Override
    public Locale getLocaleOfUser(IUser user) {
        IConfigNode customLangNode = getConfig().getNode("customLangs", user.getClientUniqueID());

        if (!customLangNode.isVirtual()) {
            return getLocaleForCountryId(customLangNode.asString());

        } else {
            return this.getFallbackLocale();
        }
    }

    @Override
    public void setLocaleForClient(String clientUniqueId, Locale locale) {
        if (locale == null) {
            unsetLocaleForClient(clientUniqueId);
        } else {
            getConfig().getNode("customLangs", clientUniqueId).setString(locale.toLanguageTag());
        }
    }

    private void unsetLocaleForClient(String clientUniqueId) {
        getConfig().getNode("customLangs").remove(clientUniqueId);
    }

    @Override
    public Locale getFallbackLocale() {
        return Locale.forLanguageTag(getConfig().getNode("fallbackLocale").optString("en"));
    }

    @Override
    protected IConfig getConfigRef() {
        return localizationConfig;
    }

    @Override
    protected String getDefaultResource() {
        return DEFAULT_CONFIG;
    }

    @Override
    protected boolean populateDefaultConf(IConfigNode root) {
        return false;
    }
}
