package de.fearnixx.t3;

import de.fearnixx.t3.ts3.QueryConnection;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class T3Bot implements Runnable {

    public static final String VERSION = "@VERSION@";

    private ILogReceiver log;

    private File confFile;
    private ConfigLoader loader = new JSONConfigLoader();
    private ConfigNode config;

    private boolean initCalled = false;
    private QueryConnection parentConnection;
    private List<QueryConnection> connections;

    public T3Bot(ILogReceiver log, String config) {
        this.log = log;
        this.confFile = new File(config);
    }

    @Override
    public void run() {
        log.info("Initializing T3Bot version " + VERSION);
        if (initCalled) {
            throw new RuntimeException("Reinitialization of T3Bot instances is not supported! Completely shut down the instance before and/or create a new one.");
        }
        initCalled = true;
        loader.resetError();
        loader = new JSONConfigLoader();
        config = loader.loadFromFile(confFile);
        if (loader.hasError()) {
            throw new RuntimeException("Can't read configuration!", loader.getError());
        }

        boolean rewrite = false;
        ConfigNode n = config.getNode("query", "host");
        if (n.isType(String.class)) {
            n.setValue("localhost:10011");
            rewrite = true;
        }
        n = config.getNode("query", "user");
        if (n.isType(String.class)) {
            n.setValue("serveradmin");
            rewrite = true;
        }
        n = config.getNode("query", "password");
        if (n.isType(String.class)) {
            n.setValue("adminpassword123");
            rewrite = true;
        }
        n = config.getNode("query", "serverid");
        if (n.isType(Integer.class)) {
            n.setValue(1);
            rewrite = true;
        }

        if (rewrite) {
            saveConfig();
        }

        n = config.getNode("query");
        String host = n.getNode("host").optString("localhost");
        Integer port = n.getNode("port").optInt(10011);
        parentConnection = new QueryConnection(log.getChild("pCon"), host, port);
        log.fine("Opening connection to " + host + ":" + port);
        try {
            parentConnection.open();
        } catch (IOException e) {
            log.severe("Failed to open parentConnection.", e);
            initCalled = false;
            return;
        }
        String username = n.getNode("user").optString("serveradmin");
        String password = n.getNode("password").optString("adminpassword123");
    }

    public void saveConfig() {
        loader.resetError();
        loader.save(config);
        if (loader.hasError()) {
            log.severe("Failed to save configuration: ", loader.getError().getMessage(), loader.getError());
        }
    }
}
