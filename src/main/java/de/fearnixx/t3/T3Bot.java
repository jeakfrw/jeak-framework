package de.fearnixx.t3;

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
import de.fearnixx.t3.service.task.ITaskService;
import de.fearnixx.t3.task.TaskService;
import de.fearnixx.t3.teamspeak.IServer;
import de.fearnixx.t3.teamspeak.Server;
import de.fearnixx.t3.teamspeak.cache.DataCache;
import de.fearnixx.t3.teamspeak.query.QueryConnection;
import de.fearnixx.t3.teamspeak.query.except.QueryConnectException;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

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

    // * * * VOLATILES * * * //

    private volatile boolean initCalled = false;

    // * * * FIELDS * * * //

    private Consumer<IBot> onShutdown;

    private File baseDir;
    private File logDir;
    private File confDir;
    private File confFile;

    private ILogReceiver log;
    private ConfigLoader loader;

    private ConfigNode config;

    private PluginManager pMgr;
    private InjectionManager injectionManager;
    private Map<String, PluginContainer> plugins;

    private Server server;
    private DataCache dataCache;

    private ServiceManager serviceManager;
    private EventService eventService;
    private TaskService taskService;
    private CommandService commandService;

    private final Object lock = new Object();

    // * * * CONSTRUCTION * * * //

    public T3Bot(ILogReceiver log) {
        this.log = log;
    }

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

    // * * * [Runnable] * * * //

    @Override
    public void run() {
        log.info("Initializing T3Bot version " + VERSION);

        if (initCalled) {
            throw new RuntimeException("Reinitialization of T3Bot instances is not supported! Completely shut down beforehand and/or create a new one.");
        }

        // Bot Pre-Initialization
        setBaseDir(confFile.getAbsoluteFile().getParentFile().getParentFile());
        initCalled = true;
        plugins = new HashMap<>();

        // Create services and register them
        log.fine("Constructing services");
        serviceManager = new ServiceManager();
        eventService = new EventService(log.getChild("EM"));
        taskService = new TaskService(log.getChild("TM"), (pMgr.estimateCount() > 0 ? pMgr.estimateCount() : 10) * 10);
        commandService = new CommandService(log.getChild("!CM"));
        injectionManager = new InjectionManager(log, serviceManager);
        injectionManager.setBaseDir(getDir());
        server = new Server(eventService, log.getChild("SVR"));

        serviceManager.registerService(IBot.class, this);
        serviceManager.registerService(IServiceManager.class, serviceManager);
        serviceManager.registerService(IEventService.class, eventService);
        serviceManager.registerService(ITaskService.class, taskService);
        serviceManager.registerService(ICommandService.class, commandService);
        serviceManager.registerService(IInjectionService.class, injectionManager);
        serviceManager.registerService(IServer.class, server);

        injectionManager.injectInto(serviceManager);
        injectionManager.injectInto(eventService);
        injectionManager.injectInto(taskService);
        injectionManager.injectInto(commandService);
        injectionManager.injectInto(server);

        taskService.start();

        pMgr.load(true);
        Map<String, PluginRegistry> regMap = pMgr.getAllPlugins();
        // Load all plugins - This is where dependencies are being enforced
        regMap.forEach((n, pr) -> loadPlugin(regMap, n, pr));
        StringBuilder b = new StringBuilder();
        plugins.forEach((k, v) -> b.append(k).append(", "));
        log.info("Loaded ", plugins.size(), " plugin(s): ", b.toString());
        eventService.fireEvent(new BotStateEvent.PluginsLoaded().setBot(this));

        // Initialize Bot configuration and Plugins
        BotStateEvent.Initialize event = ((BotStateEvent.Initialize) new BotStateEvent.Initialize().setBot(this));
        initializeConfiguration(event);
        eventService.fireEvent(event);
        if (event.isCanceled()) {
            log.warning("An initialization task has requested the bot to cancel startup. Doing that.");
            shutdown();
            return;
        }

        String host = config.getNode("host").getString();
        Integer port = config.getNode("port").getInt();
        String user = config.getNode("user").getString();
        String pass = config.getNode("pass").getString();
        Integer instID = config.getNode("instance").getInt();
        eventService.fireEvent(new BotStateEvent.PreConnect().setBot(this));

        try {
            server.connect(host, port, user, pass, instID);
        } catch (QueryConnectException e) {
            log.severe("Failed to start bot: TS3INIT failed", e);
            shutdown();
            return;
        }

        Boolean doNetDump = Main.getProperty("bot.connection.netdump", Boolean.FALSE);

        if (doNetDump) {
            File netDumpFile = new File(logDir, "bot_" + instID);
            if (!netDumpFile.isDirectory())
                netDumpFile.mkdirs();
            netDumpFile = new File(netDumpFile, "net_dump.main.log");
            ((QueryConnection) server.getConnection()).setNetworkDump(netDumpFile);
        }

        server.getConnection().setNickName(config.getNode("nick").optString(null));
        dataCache = new DataCache(log.getChild("cache"), server.getConnection(), eventService);
        dataCache.scheduleTasks(taskService);

        eventService.registerListeners(commandService);
        eventService.registerListener(dataCache);

        log.info("Connected");
        eventService.fireEvent(new BotStateEvent.PostConnect().setBot(this));
    }

    private boolean loadPlugin(Map<String, PluginRegistry> reg, String id, PluginRegistry pr) {
        ILogReceiver log  = this.log.getChild("PLINIT");
        log.fine("Loading plugin ", id);
        PluginContainer c = plugins.getOrDefault(id, null);

        if (c == null) c = pr.newContainer();
        plugins.put(id, c);

        if (c.getState() == PluginContainer.State.DEPENDENCIES) {
            log.severe("Possible circular dependency detected! Plugin ", id, " was resolving dependencies when requested!");
            return true;
        } else if (c.getState() == PluginContainer.State.DONE) {
            log.finer("Plugin already initialized - Skipping");
            return true;
        } else if (c.getState() != PluginContainer.State.INIT) {
            throw new IllegalStateException(new ConcurrentModificationException("Attempt to load plugin in invalid state!"));
        }

        c.setState(PluginContainer.State.DEPENDENCIES);

        for (String dep : pr.getHARD_depends()) {
            if (!reg.containsKey(dep) || !loadPlugin(reg, dep, reg.get(dep))) {
                log.severe("Unresolvable HARD dependency: ", dep, " for plugin ", id);
                c.setState(PluginContainer.State.FAILED_DEP);
                return false;
            }
        }

        c.setState(PluginContainer.State.INJECT);

        try {
            c.construct();
        } catch (Throwable e) {
            log.severe("Failed to construct plugin: ", id, e);
            c.setState(PluginContainer.State.FAILED);
            return false;
        }

        injectionManager.injectInto(c.getPlugin(), id);
        if (c.getListener().hasAny())
            eventService.addContainer(c.getListener());
        if (c.getSystemListener().hasAny())
            eventService.addContainer(c.getSystemListener());
        c.setState(PluginContainer.State.DONE);
        log.fine("Initialized plugin ", id);
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
                log.severe("Can't read configuration! " + confFile.getPath(), loader.getError());
                event.cancel();
                return;
            }
        } else {
            log.warning("Creating new default configuration! Requesting shutdown after initialization.");
            config = new ConfigNode();
            event.cancel();
        }

        boolean rewrite = false;
        boolean importantIsDefault = false;

        if (!config.getNode("host").isType(String.class)) {
            config.getNode("host").setValue("localhost");
            rewrite = true;
        }
        if (!config.getNode("port").isType(Integer.class)) {
            config.getNode("port").setValue(10011);
            rewrite = true;
        }
        if (!config.getNode("user").isType(String.class)) {
            config.getNode("user").setValue("serveradmin");
            rewrite = true;
        }
        if (!config.getNode("pass").isType(String.class)) {
            config.getNode("pass").setValue("password");
            rewrite = true;
            importantIsDefault = true;
        }
        if (!config.getNode("instance").isType(Integer.class)) {
            config.getNode("instance").setValue(1);
            rewrite = true;
            importantIsDefault = true;
        }

        if (rewrite) {
            loader.resetError();
            loader.save(config);
            if (loader.hasError()) {
                log.severe("Failed to rewrite configuration. Aborting startup, just in case.", loader.getError());
                event.cancel();
            }
        }
        if (importantIsDefault) {
            log.warning("One or more important settings are default values. Please review the configuration at: ", confFile.toURI().toString());
            event.cancel();
        }
    }

    public void saveConfig() {
        loader.resetError();
        loader.save(config);
        if (loader.hasError()) {
            log.severe("Failed to save configuration: ", loader.getError().getMessage(), loader.getError());
        }
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        log.fine("Base directory changed to: ", baseDir.toString());
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
        log.fine("Log directory changed to: ", logDir.toString());
    }

    public void setConfDir(File confDir) {
        this.confDir = confDir;
        log.fine("Conf directory changed to: ", confDir.toString());
    }

    // * * * MISC * * * //

    public File getDir() {
        return baseDir;
    }

    public File getConfDir() {
        return confDir;
    }

    public File getLogDir() {
        return logDir;
    }

    public IServiceManager getServiceManager() {
        return serviceManager;
    }

    public IEventService getEventService() {
        return eventService;
    }

    public ITaskService getTaskService() {
        return taskService;
    }

    public ICommandService getCommandService() { return commandService; }

    public IServer getServer() {
        return server;
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
