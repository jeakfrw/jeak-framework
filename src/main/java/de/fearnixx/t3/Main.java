package de.fearnixx.t3;

import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;
import de.mlessmann.logging.MarkL4YGLogger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class Main {

    public static void main() {
        new Main().run();
    }

    private MarkL4YGLogger logger = MarkL4YGLogger.get("t3");
    private ConfigLoader loader;
    private ConfigNode config;

    public Main() {
        ILogReceiver r = logger.getLogReceiver();

        loader = new JSONConfigLoader();
        File f = new File("t3.json");
        config = loader.loadFromFile(f);
        if (loader.hasError()) {
            r.severe("Cannot open configuration: ", loader.getError().getMessage());
            loader.getError().printStackTrace();
            System.exit(1);
        }
        Optional<Map<String,ConfigNode>> bots = config.getNode("bots").getHub();
        if (!bots.isPresent()) {
            System.err.println("No bots configured");
            config.getNode("bot_0").getNode("config").setValue("configs/bot_0");
            new File("config/bot_0").mkdirs();
            loader.save(config);
            System.exit(1);
        } else {
            Map<String, ConfigNode> nodes = bots.get();
        }
    }

    public static void run() {

    }
}
