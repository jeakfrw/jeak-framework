package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.mlessmann.confort.api.IConfigNode;

import java.io.File;
import java.util.Locale;

/**
 * Localization units define unrelated parts of language configurations.
 * Plugins, Services or even features can use separate localization units just as they do with configuration files.
 */
public interface ILocalizationUnit {

    /**
     * Returns the associated unit identifier.
     * This is likely to be the plugin or service id in most cases.
     */
    String getUnitId();

    /**
     * Returns a context for the given client.
     * This is the recommended way of doing so as this respects the clients custom language settings.
     */
    ILocaleContext getContext(IClient client);

    /**
     * Returns a context for the given user.
     * This is the recommended way of doing so as this respects the users custom language settings.
     */
    ILocaleContext getContext(IUser user);

    /**
     * Returns a context for the given locale.
     * If a context for the locale is not available, the default context will be returned.
     */
    ILocaleContext getContext(Locale locale);

    /**
     * Returns the context for the given country code.
     * If a context is not available or the code could not be translated into a {@link Locale}, the default context will be returned.
     * <em>Please avoid using this from version 1.1.0 and up. Use {@link #getContext(IClient)} instead.</em>
     */
    ILocaleContext getContext(String ts3CountryCode);

    /**
     * Instructs the unit to load the specified (classpath) resource URI and merge the values with already configured ones.
     * When the unit is not persisted yet, this will simply copy the templates from the ressource.
     *
     * Fails silently when the resource could not be loaded. (Errors will be logged)
     */
    void loadDefaultsFromResource(ClassLoader loader, String resourceURI);

    /**
     * Instructs the unit to load the specified {@link File} and merge the values with already configured ones.
     * When the unit is not persisted yet, this will simply copy the templates from the resource.
     *
     * Fails silently when the resource could not be loaded. (Errors will be logged)
     */
    void loadDefaultsFromFile(File file);

    /**
     * Instructs the unit to merge the values with already configured ones.
     * When the unit is not persisted yet, this will simply copy the templates from the node.
     *
     * The unit will read the keys "default" and "langs".
     * <ul>
     *     <li><strong>defaultLanguage</strong> being the country or locale code for the default context. E.g. "en"</li>
     *     <li><strong>"langs" containing keys which are either country or locale codes with a mapping from message id to template below.</strong></li>
     * </ul>
     */
    void loadDefaultsFromNode(IConfigNode configNode);
}
