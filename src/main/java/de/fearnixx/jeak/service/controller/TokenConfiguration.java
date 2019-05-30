package de.fearnixx.jeak.service.controller;

import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.controller.reflect.RestController;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Optional;

public class TokenConfiguration extends Configurable {
    private static final Logger logger = LoggerFactory.getLogger(TokenConfiguration.class);
    private static final String DEFAULT_TOKEN_CONFIG ="restService/token/defaultToken.json";

    @Inject
    @Config(id = "tokens")
    private IConfig configRef;

    public TokenConfiguration() {
        super(TokenConfiguration.class);
    }

    @Override
    public boolean loadConfig() {
        return super.loadConfig();
    }

    @Override
    protected void onDefaultConfigLoaded() {
        saveConfig();
    }

    @Override
    protected IConfig getConfigRef() {
        return configRef;
    }

    @Override
    protected String getDefaultResource() {
        return DEFAULT_TOKEN_CONFIG;
    }

    @Override
    protected boolean populateDefaultConf(IConfigNode root) {
        return false;
    }

    /**
     * The token path is defined as : pluginId -> controllerName -> token
     * @param clazz The class of the controller.
     * @return The token as {@link Optional<String>} if the path was resolved. Otherwise an empty {@link Optional<String>}.
     */
    public Optional<String> readToken(Class<?> clazz) {
        logger.debug(MessageFormat.format("reading token for {0}", clazz.getName()));
        return getConfig().getNode(clazz.getAnnotation(RestController.class).pluginId()).getNode(clazz.getName()).getNode("restService/token").optString();
    }
}
