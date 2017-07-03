package de.fearnixx.t3;

import de.fearnixx.t3.commandline.CommandLine;
import de.fearnixx.t3.reflect.plugins.persistent.PluginManager;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;
import de.mlessmann.logging.MarkL4YGLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class Main {

    private static Main INST;

    public static Main getInstance() {
        return INST == null ? (INST = new Main()) : INST;
    }

    public static void main(String[] args) {
        getInstance().run();
    }

    private MarkL4YGLogger logger = MarkL4YGLogger.get(" ");
    private ConfigLoader loader;
    private ConfigNode config;
    private List<T3Bot> t3bots = new ArrayList<>();
    private PluginManager mgr;
    private CommandLine cmd;

    public Main() {
    }

    public void run() {
        logger.setLevel(Level.FINEST);
        logger.setLogTrace(false);
        logger.disableErrOut();
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
            config.getNode("bots", "bot_0").getNode("config").setValue("configs/bot_0");
            new File("config/bot_0").mkdirs();
            loader.save(config);
            System.exit(1);
        } else {
            mgr = new PluginManager(logger.getLogReceiver().getChild("PMGR"));;
            mgr.addSource(new File("plugins"));

            Map<String, ConfigNode> nodes = bots.get();
            nodes.forEach((k, node) -> {
                String confPath = config.getNode("config").optString(k + "/config/bot.json");
                File botConf = new File(confPath);
                if (!botConf.exists()) {
                    botConf.getAbsoluteFile().getParentFile().mkdirs();
                    try {
                        botConf.createNewFile();
                    } catch (IOException e) {
                        r.severe("Cannot start bot: ", e);
                        return;
                    }
                }
                T3Bot bot = new T3Bot(logger.getLogReceiver().getChild(k));
                bot.setPluginManager(mgr);
                bot.setConfig(botConf);
                t3bots.add(bot);
                new Thread(bot, k).start();
            });
        }

        cmd = new CommandLine(System.in, System.out, logger.getLogReceiver().getChild("CM"));
        cmd.run();
    }

    public void shutdown() {
        cmd.kill();
        t3bots.forEach(T3Bot::shutdown);
    }
}
