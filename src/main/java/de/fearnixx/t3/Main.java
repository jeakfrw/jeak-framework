package de.fearnixx.t3;

import de.fearnixx.t3.commandline.CommandLine;
import de.fearnixx.t3.plugin.persistent.PluginManager;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import de.mlessmann.confort.config.FileConfig;
import de.mlessmann.confort.lang.ConfigLoader;
import de.mlessmann.confort.lang.json.JSONConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Main INST;

    public static Main getInstance() {
        return INST == null ? (INST = new Main()) : INST;
    }

    public static void main(String[] args) {
        getInstance().run(args);
    }

    private IConfig configRef;
    private IConfigNode config;
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

        /* TODO: Log4J Setup!
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
        */

        for (int i = 0; i < args.length; i++) {
            logger.info("ARG: {}", args[i]);
        }

        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < jvmArgs.size(); i++) {
            logger.info("JVM_ARG: {}", jvmArgs.get(i));
        }

        ConfigLoader loader = new JSONConfigLoader();
        File mainConfig = new File("t3serverbot.json");

        try {
            configRef = new FileConfig(new JSONConfigLoader(), mainConfig);
            configRef.load();
            config = configRef.getRoot();
        } catch (IOException | ParseException e) {
            logger.error("Failed to open configuration!", e);
            System.exit(1);
        }

        if (!config.getNode("bots").isMap()) {
            logger.warn("No bots configured");
            config.getNode("bots", "main").getNode("config").setString("main/config/bot.json");
            config.getNode("bots", "main").getNode("base-dir").setString("main");
            new File("main").mkdirs();

            try {
                configRef.save();
            } catch (IOException e) {
                logger.error("Failed to save defaul configuration.", e);
            }
            System.exit(1);
        } else {
            mgr = new PluginManager();
            mgr.addSource(new File("plugins"));
            mgr.addSource(new File("libraries"));


            Map<String, IConfigNode> bots = config.getNode("bots").asMap();
            bots.forEach((k, node) -> {
                String confPath = node.getNode("config").optString().orElse(k + "/config/bot.json");
                File botConfFile = new File(confPath);

                File absoluteConfParent = botConfFile.getAbsoluteFile().getParentFile();
                if (!absoluteConfParent.isDirectory() && !absoluteConfParent.mkdirs()) {
                    logger.error("Cannot run mkdirs for bot: {} -> {}", k, absoluteConfParent.getPath());
                    return;
                }

                IConfig botConf = new FileConfig(new JSONConfigLoader(), botConfFile);

                T3Bot bot = new T3Bot();
                bot.setLogDir(new File("logs"));
                bot.setBaseDir(new File(node.getNode("base-dir").optString().orElse(k)));
                bot.setConfDir(new File(bot.getBaseDirectory(), "config"));
                bot.setConfig(botConf);
                bot.setBotInstanceID(k);
                bot.setPluginManager(mgr);
                bot.onShutdown(this::onBotShutdown);
                t3bots.add(bot);
                new Thread(bot, k).start();
            });
        }

        cmd = new CommandLine(System.in, System.out);
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
            logger.warn("Shutdown sleep interrupted!", e);
        }

        List<Thread> runningThreads = new LinkedList<>(Thread.getAllStackTraces().keySet());
        logger.info("{} threads running upon shutdown.", runningThreads.size());

        for (Thread thread : runningThreads) {
            StackTraceElement[] trace = thread.getStackTrace();
            String position = "No position available";

            if (trace.length > 0)
                position = "(" + trace[0].getClassName() + ':' + trace[0].getLineNumber() + ')';

            logger.debug("Running thread on shutdown: [", thread.getState().toString(), "] ",
                    thread.getId(), '/', thread.getName(), " @ ", position);
        }
    }

    /**
     * Warning "unchecked" suppressed: Checks are performed!
     * <p>
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
