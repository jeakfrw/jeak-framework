package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.controller.IRestControllerService;
import de.fearnixx.jeak.service.controller.RegisterControllerException;
import de.fearnixx.jeak.service.controller.testImpls.SecondTestController;
import de.fearnixx.jeak.service.controller.testImpls.TestController;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import de.mlessmann.confort.api.IConfig;

@JeakBotPlugin(id = "controllertestplugin")
public class ControllerTestPlugin extends AbstractTestPlugin {

    @Inject
    IRestControllerService restControllerService;


    public ControllerTestPlugin() {
        super();
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        restControllerService.registerController(TestController.class, new TestController());
        try {
            restControllerService.registerController(SecondTestController.class, new SecondTestController());
        } catch (RegisterControllerException e) {
            success("Should fail to register a duplicated controller");
        }

    }
}
