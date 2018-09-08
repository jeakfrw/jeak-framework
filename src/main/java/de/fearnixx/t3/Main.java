package de.fearnixx.t3;

import de.fearnixx.t3.commandline.CommandLine;
import de.fearnixx.t3.database.DatabaseService;
import de.fearnixx.t3.plugin.persistent.PluginManager;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
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
        getInstance().run(args);
    }

    private LogWrapper logger = new LogWrapper("", true);
    private ConfigNode config;
    private List<T3Bot> t3bots = new ArrayList<>();
    private PluginManager mgr;
    private CommandLine cmd;

    private Level commonLogLevel = Level.ALL;
    private Level consoleLogLevel = commonLogLevel;
    private Level fileLogLevel = commonLogLevel;

    private final Object lock = new Object();

    public Level parseLogLevel(String level) {
        try {
            return level != null ? Level.parse(level) : commonLogLevel;
        } catch (IllegalArgumentException ex) {
            return commonLogLevel;
        }
    }

    public Main() {
    }

    public void run(String... args) {
        commonLogLevel = parseLogLevel(getProperty("bot.loglevel", null));
        consoleLogLevel = parseLogLevel(getProperty("bot.loglevel.console", null));
        fileLogLevel = parseLogLevel(getProperty("bot.loglevel.file", null));

        logger.getLogger().setLevel(Level.ALL);
        LogFormatter formatter = new LogFormatter();
        formatter.setDebug(false);
        ConsoleHandler consoleHandler = new ConsoleHandler(System.out);
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(consoleLogLevel);
        logger.addHandler(consoleHandler);

        ILogReceiver log = logger.getLogReceiver();
        File logDir = new File("logs");

        if (logDir.isDirectory() || (!logDir.isDirectory() && logDir.mkdirs())) {
            // TODO: Add actual file logging
            FileHandler logFileHandler = new FileHandler(new File(logDir, "latest.log"), true);
            logFileHandler.setFormatter(formatter);
            try {
                logFileHandler.setLevel(fileLogLevel);
                logFileHandler.open();
                logger.addHandler(logFileHandler);
            } catch (IOException e) {
                log.severe("Failed to open log file!", e);
            }
        } else {
            log.warning("Cannot enable file logging into dir: ", logDir.getAbsoluteFile().getPath());
        }


        for (int i = 0; i < args.length; i++) {
            log.info("ARG: ", args[i]);
        }

        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < jvmArgs.size(); i++) {
            log.info("JVM_ARG: ", jvmArgs.get(i));
        }

        ConfigLoader loader = new JSONConfigLoader();
        File mainConfig = new File("t3serverbot.json");
        config = loader.loadFromFile(mainConfig);
        if (loader.hasError()) {
            log.severe("Cannot open configuration: ", loader.getError().getMessage());
            loader.getError().printStackTrace();
            System.exit(1);
        }
        Optional<Map<String,ConfigNode>> bots = config.getNode("bots").getHub();
        if (!bots.isPresent()) {
            logger.getLogger().warning("No bots configured");
            config.getNode("bots", "main").getNode("config").setValue("main/config/bot.json");
            config.getNode("bots", "main").getNode("base-dir").setValue("main");
            new File("main").mkdirs();
            loader.save(config);
            System.exit(1);
        } else {
            mgr = new PluginManager(logger.getLogReceiver().getChild("PMGR"));
            File pluginSource = new File("plugins");
            mgr.addSource(pluginSource);

            Map<String, ConfigNode> nodes = bots.get();
            nodes.forEach((k, node) -> {
                String confPath = node.getNode("config").optString(k + "/config/bot.json");
                File botConf = new File(confPath);
                if (!botConf.exists() && !botConf.getAbsoluteFile().getParentFile().mkdirs()) {
                    log.severe("Cannot start bot: " + k);
                    return;
                }
                T3Bot bot = new T3Bot(logger.getLogReceiver().getChild(k));
                bot.setLogDir(new File("logs"));
                bot.setBaseDir(new File(node.getNode("base-dir").optString(k)));
                bot.setConfDir(new File(bot.getBaseDirectory(),"config"));
                bot.setConfig(botConf);
                bot.setBotInstanceID(k);
                bot.setPluginManager(mgr);
                bot.onShutdown(this::onBotShutdown);
                t3bots.add(bot);
                new Thread(bot, k).start();
            });
        }

        cmd = new CommandLine(System.in, System.out, logger.getLogReceiver().getChild("CM"));
        cmd.run();
    }

    private void onBotShutdown(IBot bot) {
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
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            logger.getLogReceiver().warning("Shutdown sleep interrupted!", e);
        }

        List<Thread> runningThreads = new LinkedList<>(Thread.getAllStackTraces().keySet());
        logger.getLogReceiver().info(runningThreads.size(), " threads running upon shutdown.");

        for (Thread thread : runningThreads) {
            StackTraceElement[] trace = thread.getStackTrace();
            String position = "No position available";

            if (trace.length > 0)
                position = "(" + trace[0].getClassName() + ':' + trace[0].getLineNumber() + ')';

            logger.getLogReceiver().finer("Running thread on shutdown: [", thread.getState().toString(), "] ",
                    thread.getId(), '/', thread.getName(), " @ ", position);
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
