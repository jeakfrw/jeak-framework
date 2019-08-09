package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.controller.IRestControllerService;
import de.fearnixx.jeak.service.controller.exceptions.RegisterControllerException;
import de.fearnixx.jeak.service.controller.testimpls.SecondTestController;
import de.fearnixx.jeak.service.controller.testimpls.TestController;
import de.fearnixx.jeak.test.AbstractTestPlugin;

@JeakBotPlugin(id = "controllertestplugin")
public class ControllerTestPlugin extends AbstractTestPlugin {

    @Inject
    IRestControllerService restControllerService;


    public ControllerTestPlugin() {
        super();
        addTest("register_successful");
        addTest("register_duplicated_controller");
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        restControllerService.registerController(TestController.class, new TestController());
        success("register_successful");
        try {
            restControllerService.registerController(SecondTestController.class, new SecondTestController());
        } catch (RegisterControllerException e) {
            success("register_duplicated_controller");
        }

    }
}
