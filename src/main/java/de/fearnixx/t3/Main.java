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

    private final Object lock = new Object();

    public Main() {
    }

    public void run() {
        logger.setLevel(Level.FINEST);
        logger.setLogTrace(false);
        logger.disableErrOut();
        ILogReceiver r = logger.getLogReceiver();
        File logDir = new File("logs");
        if (!logDir.isDirectory() && logDir.mkdirs()) {
            // TODO: Add actual file logging
        }

        loader = new JSONConfigLoader();
        File mainConfig = new File("t3serverbot.json");
        config = loader.loadFromFile(mainConfig);
        if (loader.hasError()) {
            r.severe("Cannot open configuration: ", loader.getError().getMessage());
            loader.getError().printStackTrace();
            System.exit(1);
        }
        Optional<Map<String,ConfigNode>> bots = config.getNode("bots").getHub();
        if (!bots.isPresent()) {
            logger.getLogger().warning("No bots configured");
            config.getNode("bots", "bot_0").getNode("config").setValue("configs/bot_0");
            new File("bot_0").mkdirs();
            loader.save(config);
            System.exit(1);
        } else {
            mgr = new PluginManager(logger.getLogReceiver().getChild("PMGR"));
            mgr.addSource(new File("plugins"));

            Map<String, ConfigNode> nodes = bots.get();
            nodes.forEach((k, node) -> {
                String confPath = config.getNode("config").optString(k + "/config/bot.json");
                File botConf = new File(confPath);
                if (!botConf.exists() && !botConf.getAbsoluteFile().getParentFile().mkdirs()) {
                    r.severe("Cannot start bot: ");
                    return;
                }
                T3Bot bot = new T3Bot(logger.getLogReceiver().getChild(k));
                bot.setLogDir(new File("logs"));
                bot.setBaseDir(new File(k));
                bot.setConfDir(new File(bot.getDir(),"config"));
                bot.setConfig(botConf);
                bot.setPluginManager(mgr);
                bot.onShutdown(this::onBotShutdown);
                t3bots.add(bot);
                new Thread(bot, k).start();
            });
        }

        cmd = new CommandLine(System.in, System.out, logger.getLogReceiver().getChild("CM"));
        cmd.run();
    }

    private void onBotShutdown(IT3Bot bot) {
        if (bot instanceof T3Bot) {
            synchronized (lock) {
                t3bots.remove(bot);
                if (t3bots.isEmpty())
                    shutdown();
            }
        }
    }

    public void shutdown() {
        cmd.kill();
        for (int i = t3bots.size() - 1; i >= 0; i--) {
            t3bots.get(i).shutdown();
        }
    }

    /**
     * Warning "unchecked" suppressed: Checks are performed!
     *
     * Char is not supported - use string .-.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(String name, T def) {
        try {
            String value = System.getProperty(name);
            if (value != null) {
                if (def == null || def.getClass().isAssignableFrom(String.class)) {
                    return (T) value;
                } else {
                    Class<?> defClass = def.getClass();

                    if (defClass.isAssignableFrom(Double.class)) {
                        return (T) Double.valueOf(value);
                    }
                    if (defClass.isAssignableFrom(Float.class)) {
                        return (T) Float.valueOf(value);
                    }
                    if (defClass.isAssignableFrom(Long.class)) {
                        return (T) Long.valueOf(value);
                    }
                    if (defClass.isAssignableFrom(Integer.class)) {
                        return (T) Integer.valueOf(value);
                    }
                    if (defClass.isAssignableFrom(Boolean.class)) {
                        return (T) Boolean.valueOf(value);
                    }
                    if (defClass.isAssignableFrom(Enum.class)) {
                        return (T) Enum.valueOf((Class<Enum>) defClass, value);
                    }
                }
            }
        } catch (Exception e) {
        }
        return def;
    }
}
