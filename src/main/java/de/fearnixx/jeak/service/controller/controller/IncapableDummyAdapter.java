package de.fearnixx.jeak.service.controller.controller;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.service.controller.connection.HttpServer;
import de.fearnixx.jeak.service.controller.connection.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncapableDummyAdapter extends HttpServer {

    public static final boolean EXPERIMENTAL_REST_ENABLED = Main.getProperty("jeak.experimental.enable_rest", false);

    private static final Logger logger = LoggerFactory.getLogger(IncapableDummyAdapter.class);

    public IncapableDummyAdapter(RestConfiguration restConfiguration) {
        super(restConfiguration);
        logger.warn("REST functionality is experimental and therefore disabled by default. Enable with \"jeak.experimental.enable_rest\"");
    }

    @Override
    public void registerController(ControllerContainer controllerContainer) {
        // This adapter is just a dummy until the REST service has been released.
    }
}
