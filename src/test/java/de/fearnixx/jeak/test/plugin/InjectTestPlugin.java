package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import de.mlessmann.confort.api.IConfig;

import javax.persistence.EntityManager;

@JeakBotPlugin(id = "conforttest")
public class InjectTestPlugin extends AbstractTestPlugin {

    @Inject
    @Config
    public IConfig configRef;

    @Inject
    public IDataCache dataCache;

    public InjectTestPlugin() {
        super();
        addTest("test_injected_configRef");
        addTest("test_injected_dataCache");
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        if (configRef != null) {
            success("test_injected_configRef");
        }

        if (dataCache != null) {
            success("test_injected_dataCache");
        }
    }
}
