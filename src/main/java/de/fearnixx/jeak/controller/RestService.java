package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.connection.HttpServer;
import de.fearnixx.jeak.controller.controller.ControllerContainer;
import de.fearnixx.jeak.controller.events.IControllerEvent;
import de.fearnixx.jeak.controller.interfaces.IRestService;
import de.fearnixx.jeak.reflect.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestService implements IRestService {
    private static final Logger logger = LoggerFactory.getLogger(RestService.class);
    private HttpServer httpServer;


    public RestService() {
        this.httpServer = new HttpServer();
    }

    @Listener
    public void methodRegistered(IControllerEvent.IControllerRegistered controllerRegisteredEvent) {
        ControllerContainer controllerContainer = new ControllerContainer(controllerRegisteredEvent.getController());
        try {
            httpServer.registerController(controllerContainer);
        } catch (Exception e) {
            logger.error("there was an error while registering the controller", e);
        }
    }
}
