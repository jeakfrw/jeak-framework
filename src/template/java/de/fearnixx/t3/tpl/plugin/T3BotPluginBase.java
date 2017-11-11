package de.fearnixx.t3.tpl.plugin;

import de.fearnixx.t3.IT3Bot;
import de.fearnixx.t3.event.state.IBotStateEvent;
import de.fearnixx.t3.reflect.annotation.Inject;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

/**
 * A template class which you can extend to not having to deal with ALL the stuff all over again
 *
 * Created by MarkL4YG on 10-Nov-17
 */
public abstract class T3BotPluginBase {

    @Inject
    public ILogReceiver myLogger;

    @Inject(id = "main")
    public ConfigLoader myConfigLoader;
    protected ConfigNode myConfig;

    @Inject
    public IT3Bot myBot;

    protected void loadConfig() {
        myConfigLoader.resetError();
        myConfig = myConfigLoader.load();
        if (myConfigLoader.hasError()) {
            myLogger.warning("Failed to load configuration!", myConfigLoader.getError());
            myConfig = null;
        }
    }

    protected void saveConfig() {
        myConfigLoader.resetError();
        myConfigLoader.save(myConfig);
        if (myConfigLoader.hasError()) {
            myLogger.warning("Failed to save configuration!", myConfigLoader.getError());
            myConfig = null;
        }
    }

    protected ConfigNode getConfig() {
        return myConfig;
    }

    protected IT3Bot getBot() {
        return myBot;
    }
}
