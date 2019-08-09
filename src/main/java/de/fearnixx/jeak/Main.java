package de.fearnixx.jeak;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.fearnixx.jeak.commandline.CommandLine;
import de.fearnixx.jeak.plugin.persistent.PluginManager;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;
import de.mlessmann.confort.lang.RegisterLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main implements Runnable {

    private static final String CONF_FORMAT = "application/json";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Main INSTANCE = new Main();

    private final PluginManager pluginManager = new PluginManager();

    private final Executor mainExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(false)
                    .setNameFormat("main-%d")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build()
    );
    private final JeakBot jeakBot = new JeakBot();
    private final CommandLine cmd = new CommandLine(System.in, System.out);

    public static void main(String[] arguments) {
        for (String arg : arguments) {
            logger.info("ARG: {}", arg);
        }

        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String jvmArg : jvmArgs) {
            logger.info("JVM_ARG: {}", jvmArg);
        }

        getInstance().run();
    }

    /**
     * Warning "unchecked" suppressed: Checks are performed!
     *
     * <p>Char is not supported - use string .-.
     */
    @SuppressWarnings({"unchecked", "squid:S3776"})
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
            logger.error("Failed to retrieve system property: {}", name, e);
        }
        return def;
    }

    public static Main getInstance() {
        return INSTANCE;
    }

    public void run() {
        discoverPlugins();
        startBot();
        runLoop();
    }

    private void discoverPlugins() {
        pluginManager.addSource(new File("plugins"));
        pluginManager.addSource(new File("libraries"));
    }

    private void startBot() {
        final File baseDir = new File(".");
        final File confDir = new File(baseDir, "config");
        final File confFile = new File(confDir, "bot.json");
        final IConfig botConfig = createConfig(confFile);

        jeakBot.setBaseDir(baseDir);
        jeakBot.setConfDir(confDir);
        jeakBot.setConfig(botConfig);
        jeakBot.setPluginManager(pluginManager);


        jeakBot.onShutdown(this::onBotShutdown);

        mainExecutor.execute(jeakBot);
    }

    private IConfig createConfig(File confFile) {
        RegisterLoaders.registerLoaders();
        final IConfigLoader configLoader = LoaderFactory.getLoader(CONF_FORMAT);
        return new FileConfig(configLoader, confFile);
    }

    private void runLoop() {
        cmd.run();
    }

    private void onBotShutdown(JeakBot bot) {
        internalShutdown();
    }

    public void shutdown() {
        jeakBot.shutdown();
        internalShutdown();
    }

    private void internalShutdown() {
        cmd.kill();
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

            if (trace.length > 0) {
                position = "(" + trace[0].getClassName() + ':' + trace[0].getLineNumber() + ')';
            }

            logger.debug("Running thread on shutdown: [{}] {}/{} @ {}",
                    thread.getState(), thread.getId(), thread.getName(), position);
        }

        if (getProperty("bot.enableTests", false)) {
            logger.warn("===== Dumping test results =====");
            AbstractTestPlugin.getTestPlugins()
                    .forEach(plugin -> {
                        logger.info("  {}", plugin.getClass().getAnnotation(JeakBotPlugin.class).id());

                        final Map<String, Boolean> results = plugin.getResults();
                        results.forEach((name, result) -> {
                            if (result) {
                                logger.info("    {}: Success", name);
                            } else {
                                logger.error("    {}: Failure", name);
                            }
                        });
                    });
            logger.warn("===== END TEST RESULTS =====");
        }

        System.exit(0);
    }
}
