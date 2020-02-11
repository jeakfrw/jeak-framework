package de.fearnixx.jeak.util;

import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Utility/helper class for plugins or other modules that have a configuration.
 * Allows plugins to omit configuration-loading code by extending this class.
 * Supports only one configuration per instance.
 */
public abstract class Configurable  {

    private static final Charset RESOURCE_CHARSET = Charset.forName("UTF-8");
    private final Logger logger;
    private final Class<? extends Configurable> mainClass;
    private IConfigNode config;

    /**
     * @param subClass is used to instantiate the logger and retrieve the default resource.
     */
    protected Configurable(Class<? extends Configurable> subClass) {
        this.logger = LoggerFactory.getLogger(subClass);
        this.mainClass = subClass;
    }

    /**
     * Link this to the injected configuration.
     */
    protected abstract IConfig getConfigRef();

    /**
     * Returns the root configuration node.
     */
    protected IConfigNode getConfig() {
        return config;
    }

    /**
     * If you have a default configuration in your resources, return its path.
     * Otherwise return {@code null}.
     */
    protected abstract String getDefaultResource();

    /**
     * If you want to the default configuration to be populated programmatically, implement this method.
     * Otherwise, leave it empty.
     * The default configuration will be an empty node in this case.
     * @return Whether or not the population was successful.
     */
    protected abstract boolean populateDefaultConf(IConfigNode root);

    /**
     * Tries to load the configuration from {@link #getConfigRef()}.
     * If the configuration is empty,
     * will try to load using {@link #getDefaultResource()} or {@link #populateDefaultConf(IConfigNode)}.
     * @return whether or not the operation was successful and {@link #getConfig()} can be used.
     */
    protected boolean loadConfig() {
        try {
            getConfigRef().load();
            config = getConfigRef().getRoot();
        } catch (ParseException | IOException e) {
            logger.error("Failed to load configuration!", e);
        }

        if (config.isVirtual()) {
            String defConfURI = getDefaultResource();

            if (defConfURI != null) {
                logger.debug("Implementation provides default conf resource.");
                return loadFromResource(defConfURI);

            } else {
                logger.debug("Implementation does not provide a default URI. Using programmatic approach.");
                if (populateDefaultConf(config)) {
                    onDefaultConfigLoaded();
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tries to load the configuration from a specified classpath resource.
     * The resource needs to be in "application/json"-compatible format.
     * @return whether or not the configuration could be loaded successfully.
     */
    private boolean loadFromResource(String resourceURI) {
        InputStream in = mainClass.getResourceAsStream(resourceURI);

        if (in == null) {
            logger.error("Cannot load default configuration from cp URI: {}", resourceURI);
            return false;
        }

        try (InputStreamReader reader = new InputStreamReader(in, RESOURCE_CHARSET)) {
            final URI locator = URI.create("resource:" + resourceURI);
            IConfigNode defRoot = LoaderFactory.getLoader("application/json").parse(reader, locator);
            getConfigRef().setRoot(defRoot);
            config = defRoot;

            onDefaultConfigLoaded();
            return true;

        } catch (ParseException | IOException e) {
            logger.error("Failed to load default configuration!", e);
            return false;
        }
    }

    /**
     * Sub classes may override this to be notified when the default configuration has been loaded.
     * E.g. scheduling a save would be good here.
     */
    protected void onDefaultConfigLoaded() {
        // unimplemented: By default, nothing is done.
    }

    /**
     * Saves the configuration and returns whether or not that was successful.
     */
    protected boolean saveConfig() {
        try {
            getConfigRef().save();
            return true;
        } catch (IOException e) {
            logger.warn("Failed to save configuration!", e);
            return false;
        }
    }
}
