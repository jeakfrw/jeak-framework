package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.controller.IRestControllerService;
import de.fearnixx.jeak.service.controller.testImpls.TestController;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import de.mlessmann.confort.api.IConfig;

@JeakBotPlugin(id = "controllertestplugin")
public class ControllerTestPlugin extends AbstractTestPlugin {

    @Inject
    @Config
    public IConfig configRef;

    @Inject
    public IDataCache dataCache;

    @Inject
    IRestControllerService restControllerService;


    public ControllerTestPlugin() {
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
        restControllerService.registerController(TestController.class, new TestController());

    }
}
