package de.fearnixx.t3;

import de.fearnixx.t3.database.DatabaseService;
import de.fearnixx.t3.event.EventService;
import de.fearnixx.t3.event.bot.BotStateEvent;
import de.fearnixx.t3.event.bot.IBotStateEvent;
import de.fearnixx.t3.plugin.PluginContainer;
import de.fearnixx.t3.plugin.persistent.PluginManager;
import de.fearnixx.t3.plugin.persistent.PluginRegistry;
import de.fearnixx.t3.reflect.IInjectionService;
import de.fearnixx.t3.reflect.InjectionManager;
import de.fearnixx.t3.service.IServiceManager;
import de.fearnixx.t3.service.ServiceManager;
import de.fearnixx.t3.service.command.CommandService;
import de.fearnixx.t3.service.command.ICommandService;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.service.permission.base.IPermissionService;
import de.fearnixx.t3.service.permission.base.PermissionService;
import de.fearnixx.t3.service.permission.teamspeak.ITS3PermissionProvider;
import de.fearnixx.t3.service.permission.teamspeak.TS3PermissionProvider;
import de.fearnixx.t3.service.task.ITaskService;
import de.fearnixx.t3.task.TaskService;
import de.fearnixx.t3.teamspeak.IServer;
import de.fearnixx.t3.teamspeak.Server;
import de.fearnixx.t3.teamspeak.cache.DataCache;
import de.fearnixx.t3.teamspeak.cache.IDataCache;
import de.fearnixx.t3.teamspeak.except.QueryConnectException;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class T3Bot implements Runnable,IBot {

    // * * * STATICS  * * * //
    public static final Charset CHAR_ENCODING = Charset.forName("UTF-8");
    public static final String VERSION = "@VERSION@";

    private static final Logger logger = LoggerFactory.getLogger(T3Bot.class);

    // * * * VOLATILES * * * //

    private volatile boolean initCalled = false;

    // * * * FIELDS * * * //

    private Consumer<IBot> onShutdown;

    private File baseDir;
    private File logDir;
    private File confDir;
    private File confFile;
    private String botInstID;

    private ConfigLoader loader;

    private ConfigNode config;

    private PluginManager pMgr;
    private InjectionManager injectionManager;
    private Map<String, PluginContainer> plugins;

    private Server server;
    private DataCache dataCache;

    private EventService eventService;
    private TaskService taskService;
    private CommandService commandService;

    // * * * CONSTRUCTION * * * //

    public void setConfig(File confFile) {
        if (this.confFile != null)
            throw new IllegalStateException("Cannot change config once set!");
        this.confFile = confFile;
    }

    public void setPluginManager(PluginManager pMgr) {
        if (this.pMgr != null)
            throw new IllegalStateException("Cannot change pluginManager once set!");
        this.pMgr = pMgr;
    }

    public void setBotInstanceID(String instanceID) {
        this.botInstID = instanceID;
    }

    // * * * [Runnable] * * * //

    @Override
    public void run() {
        logger.info("Initializing T3Bot version {}", VERSION);

        if (initCalled) {
            throw new RuntimeException("Reinitialization of T3Bot instances is not supported! Completely shut down beforehand and/or create a new one.");
        }

        // Bot Pre-Initialization
        initCalled = true;
        plugins = new HashMap<>();

        // Create services and register them
        logger.debug("Constructing services");
        ServiceManager serviceManager = new ServiceManager();
        PermissionService permissionService = new PermissionService();
        TS3PermissionProvider ts3permissionProvider = new TS3PermissionProvider();
        DatabaseService databaseService = new DatabaseService(new File(confDir, "databases"));

        eventService = new EventService();
        taskService = new TaskService( (pMgr.estimateCount() > 0 ? pMgr.estimateCount() : 10) * 10);
        commandService = new CommandService();
        injectionManager = new InjectionManager(serviceManager);
        injectionManager.setBaseDir(getBaseDirectory());
        server = new Server(eventService);
        dataCache = new DataCache(server.getConnection(), eventService);

        serviceManager.registerService(PluginManager.class, pMgr);
        serviceManager.registerService(IBot.class, this);
        serviceManager.registerService(IServiceManager.class, serviceManager);
        serviceManager.registerService(IEventService.class, eventService);
        serviceManager.registerService(ITaskService.class, taskService);
        serviceManager.registerService(ICommandService.class, commandService);
        serviceManager.registerService(IInjectionService.class, injectionManager);
        serviceManager.registerService(IServer.class, server);
        serviceManager.registerService(IDataCache.class, dataCache);
        serviceManager.registerService(IPermissionService.class, permissionService);
        serviceManager.registerService(ITS3PermissionProvider.class, ts3permissionProvider);
        serviceManager.registerService(DatabaseService.class, databaseService);

        injectionManager.injectInto(serviceManager);
        injectionManager.injectInto(eventService);
        injectionManager.injectInto(taskService);
        injectionManager.injectInto(commandService);
        injectionManager.injectInto(server);
        injectionManager.injectInto(permissionService);
        injectionManager.injectInto(ts3permissionProvider);
        injectionManager.injectInto(databaseService);

        taskService.start();

        pMgr.setIncludeCP(true);
        pMgr.load();
        databaseService.onLoad();

        Map<String, PluginRegistry> regMap = pMgr.getAllPlugins();
        // Load all plugins - This is where dependencies are being enforced
        regMap.forEach((n, pr) -> loadPlugin(regMap, n, pr));
        StringBuilder b = new StringBuilder();
        plugins.forEach((k, v) -> b.append(k).append(", "));
        logger.info("Loaded {} plugin(s): {}", plugins.size(), b);
        eventService.fireEvent(new BotStateEvent.PluginsLoaded().setBot(this));

        // Initialize Bot configuration and Plugins
        BotStateEvent.Initialize event = ((BotStateEvent.Initialize) new BotStateEvent.Initialize().setBot(this));
        initializeConfiguration(event);
        eventService.fireEvent(event);
        if (event.isCanceled()) {
            logger.warn("An initialization task has requested the bot to cancel startup. Doing that.");
            shutdown();
            return;
        }

        String host = config.getNode("host").getString();
        Integer port = config.getNode("port").getInt();
        String user = config.getNode("user").getString();
        String pass = config.getNode("pass").getString();
        Integer ts3InstID = config.getNode("instance").getInt();
        eventService.fireEvent(new BotStateEvent.PreConnect().setBot(this));

        try {
            server.connect(host, port, user, pass, ts3InstID);
        } catch (QueryConnectException e) {
            logger.error("Failed to start bot: TS3INIT failed", e);
            shutdown();
            return;
        }

        Boolean doNetDump = Main.getProperty("bot.connection.netdump", Boolean.FALSE);

        if (doNetDump) {
            File netDumpFile = new File(logDir, botInstID);
            if (!netDumpFile.isDirectory() && !netDumpFile.mkdirs()) {
                logger.warn("Failed to enable netdump! Could not create directory: {}", netDumpFile.getPath());
            }
            netDumpFile = new File(netDumpFile, "net_dump.main.log");
//            ((QueryConnectionAccessor) server.getConnection()).setNetworkDump(netDumpFile);
        }

        server.getConnection().setNickName(config.getNode("nick").optString(null));
        dataCache.scheduleTasks(taskService);

        eventService.registerListeners(commandService);
        eventService.registerListener(dataCache);

        logger.info("Connected");
        eventService.fireEvent(new BotStateEvent.PostConnect().setBot(this));
    }

    private boolean loadPlugin(Map<String, PluginRegistry> reg, String id, PluginRegistry pr) {
        logger.info("Loading plugin {}", id);
        PluginContainer c = plugins.getOrDefault(id, null);

        if (c == null) c = pr.newContainer();
        plugins.put(id, c);

        if (c.getState() == PluginContainer.State.DEPENDENCIES) {
            logger.error("Possible circular dependency detected! Plugin {} was resolving dependencies when requested!", id);
            return true;
        } else if (c.getState() == PluginContainer.State.DONE) {
            logger.error("Plugin already initialized - Skipping");
            return true;
        } else if (c.getState() != PluginContainer.State.INIT) {
            throw new IllegalStateException(new ConcurrentModificationException("Attempt to load plugin in invalid state!"));
        }

        c.setState(PluginContainer.State.DEPENDENCIES);

        for (String dep : pr.getHARD_depends()) {
            if (!reg.containsKey(dep) || !loadPlugin(reg, dep, reg.get(dep))) {
                logger.error("Unresolvable HARD dependency: {} for plugin {}", dep, id);
                c.setState(PluginContainer.State.FAILED_DEP);
                return false;
            }
        }

        c.setState(PluginContainer.State.INJECT);

        try {
            c.construct();
        } catch (Exception e) {
            logger.error("Failed to construct plugin: ", id, e);
            c.setState(PluginContainer.State.FAILED);
            return false;
        }

        injectionManager.injectInto(c.getPlugin(), id);
        eventService.registerListener(c.getPlugin());
        c.setState(PluginContainer.State.DONE);
        logger.debug("Initialized plugin {}", id);
        return true;
    }

    // * * * Configuration * * * //

    /**
     * Initializes the bots configuration
     * Makes use of the {@link IBotStateEvent.IInitializeEvent} in order to cancel startup on unsuccessful init.
     */
    protected void initializeConfiguration(IBotStateEvent.IInitializeEvent event) {
        // Construct loader but only directly read from the file when it exists
        // Otherwise cancel startup and create default config
        loader = new JSONConfigLoader();
        loader.setFile(confFile);
        loader.setEncoding(CHAR_ENCODING);
        if (confFile.exists()) {
            config = loader.load();

            if (loader.hasError()) {
                logger.error("Can't read configuration! " + confFile.getPath(), loader.getError());
                event.cancel();
                return;
            }
        } else {
            logger.warn("Creating new default configuration! Requesting shutdown after initialization.");
            config = new ConfigNode();
            event.cancel();
        }

        boolean rewrite = false;

        rewrite = config.getNode("host").defaultValue("localhost");
        rewrite = rewrite | config.getNode("port").defaultValue(10011);
        rewrite = rewrite | config.getNode("user").defaultValue("serveradmin");
        rewrite = rewrite | config.getNode("pass").defaultValue("password");
        rewrite = rewrite | config.getNode("instance").defaultValue(1);

        if (rewrite) {
            loader.resetError();
            loader.save(config);
            if (loader.hasError()) {
                logger.error("Failed to rewrite configuration. Aborting startup, just in case.", loader.getError());
                event.cancel();
            }
            logger.warn("One or more settings have been set to default values. Please review the configuration at: {}", confFile.toURI());
            event.cancel();
        }
    }

    public void saveConfig() {
        loader.resetError();
        loader.save(config);
        if (loader.hasError()) {
            logger.error("Failed to save configuration: {} {}", loader.getError().getMessage(), loader.getError());
        }
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        logger.debug("Base directory changed to: {}", baseDir);
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
        logger.debug("Log directory changed to: {}", logDir);
    }

    public void setConfDir(File confDir) {
        this.confDir = confDir;
        logger.debug("Conf directory changed to: {}", confDir);
    }

    // * * * MISC * * * //

    @Override
    public File getBaseDirectory() {
        return baseDir;
    }

    @Override
    public IServer getServer() {
        return server;
    }

    @Override
    public IDataCache getDataCache() {
        return dataCache;
    }


    // * * * RUNTIME * * * //

    public void shutdown() {
        eventService.fireEvent(new BotStateEvent.PreShutdown().setBot(this));
        saveConfig();
        taskService.shutdown();
        commandService.shutdown();
        dataCache.reset();
        server.shutdown();
        eventService.fireEvent(new BotStateEvent.PostShutdown().setBot(this));
        eventService.shutdown();

        if (onShutdown != null)
            onShutdown.accept(this);
    }

    protected void onShutdown(Consumer<IBot> onShutdown) {
        this.onShutdown = onShutdown;
    }
}
