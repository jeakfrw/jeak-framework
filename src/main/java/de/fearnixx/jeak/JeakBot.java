package de.fearnixx.jeak;

import de.fearnixx.jeak.event.EventService;
import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.plugin.PluginContainer;
import de.fearnixx.jeak.plugin.persistent.PluginManager;
import de.fearnixx.jeak.plugin.persistent.PluginRegistry;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.IServiceManager;
import de.fearnixx.jeak.service.ServiceManager;
import de.fearnixx.jeak.service.command.CommandService;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.controller.RestControllerService;
import de.fearnixx.jeak.service.database.DatabaseService;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.locale.LocalizationService;
import de.fearnixx.jeak.service.mail.MailService;
import de.fearnixx.jeak.service.notification.NotificationService;
import de.fearnixx.jeak.service.permission.base.PermissionService;
import de.fearnixx.jeak.service.profile.ProfileService;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.service.teamspeak.UserService;
import de.fearnixx.jeak.service.token.TokenService;
import de.fearnixx.jeak.service.util.UtilCommands;
import de.fearnixx.jeak.task.TaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.Server;
import de.fearnixx.jeak.teamspeak.TS3ConnectionTask;
import de.fearnixx.jeak.teamspeak.cache.DataCache;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class JeakBot implements Runnable, IBot {

    // * * * STATICS  * * * //
    public static final Charset CHAR_ENCODING = Charset.forName("UTF-8");
    public static final String VERSION = "@VERSION@";

    private static final Logger logger = LoggerFactory.getLogger(JeakBot.class);

    // * * * VOLATILES * * * //

    private volatile boolean initCalled = false;

    // * * * FIELDS * * * //

    private Consumer<JeakBot> onShutdown;
    private final UUID instanceUUID = UUID.randomUUID();

    private File baseDir;
    private File confDir;

    private IConfig configRep;
    private IConfigNode config;

    private PluginManager pMgr;
    private ServiceManager serviceManager;
    private EventService eventService;
    private InjectionService injectionService;
    private Map<String, PluginContainer> plugins;

    private final TS3ConnectionTask connectionTask = new TS3ConnectionTask();
    private Server server;

    private final ExecutorService shutdownExecutor = Executors.newSingleThreadExecutor();

    // * * * CONSTRUCTION * * * //

    public void setConfig(IConfig configRep) {
        if (this.configRep != null)
            throw new IllegalStateException("Cannot change config once set!");
        this.configRep = configRep;
    }

    public void setPluginManager(PluginManager pMgr) {
        if (this.pMgr != null)
            throw new IllegalStateException("Cannot change pluginManager once set!");
        this.pMgr = pMgr;
    }

    // * * * [Runnable] * * * //

    @Override
    public void run() {
        logger.info("Initializing JeakBot version {}", VERSION);

        if (initCalled) {
            throw new IllegalStateException("Reinitialization of JeakBot instances is not supported! Completely shut down beforehand and/or create a new one.");
        }

        // Bot Pre-Initialization
        initCalled = true;
        plugins = new HashMap<>();
        discoverPlugins();
        doServiceStartup();
        registerFrameworkCommands();
        loadPlugins();
        eventService.fireEvent(new BotStateEvent.PreInitializeEvent().setBot(this));

        // Initialize Bot configuration and Plugins
        BotStateEvent.Initialize event = new BotStateEvent.Initialize();
        event.setBot(this);
        initializeConfiguration(event);
        if (event.isCanceled()) {
            shutdown();
            return;
        }

        eventService.fireEvent(event);
        if (event.isCanceled()) {
            logger.warn("An initialization task has requested the bot to cancel startup. Doing that.");
            shutdown();
            return;
        }

        scheduleConnect();
    }

    protected void discoverPlugins() {
        pMgr.setIncludeCP(true);
        pMgr.load();
    }

    /**
     * Initially creates and registers all services provided by the framework.
     * Generic services can be initialized via. {@link #initializeService(Object)}.
     * Crucial services that are required to be available in the initialization method must be initialized and registered first and manually.
     */
    protected void doServiceStartup() {
        // Initialize crucial services
        logger.debug("Constructing services");
        eventService = new EventService();
        serviceManager = new ServiceManager();
        serviceManager.registerService(IServiceManager.class, serviceManager);
        injectionService = new InjectionService(new InjectionContext(serviceManager, "frw", getClass().getClassLoader()));
        injectionService.addProvider(new ConfigProvider(confDir));
        injectionService.addProvider(new DataSourceProvider());
        injectionService.addProvider(new TransportProvider());
        injectionService.addProvider(new LocalizationProvider());

        serviceManager.registerService(PluginManager.class, pMgr);
        serviceManager.registerService(IBot.class, this);
        serviceManager.registerService(IServiceManager.class, serviceManager);
        serviceManager.registerService(IEventService.class, eventService);
        serviceManager.registerService(IInjectionService.class, injectionService);

        // Initialize utility & convenience services.
        server = initializeService(new Server());
        initializeService(new TaskService((pMgr.estimateCount() > 0 ? pMgr.estimateCount() : 10) * 10));
        initializeService(new DataCache());
        initializeService(new LocalizationService());
        initializeService(new CommandService());
        initializeService(new NotificationService());
        DatabaseService dbSvc = new DatabaseService(new File(confDir, "databases"));
        initializeService(dbSvc);
        MailService mailSvc = new MailService(new File(confDir, "mail"));
        initializeService(mailSvc);
        initializeService(new ProfileService(new File(confDir, "profiles")));
        initializeService(new PermissionService());
        initializeService(new UserService());
        initializeService(new TokenService());
        initializeService(new RestControllerService());

        // TODO: Remove eagerly loading by a better solution
        dbSvc.onLoad(null);
        mailSvc.onLoad(null);

        injectionService.injectInto(connectionTask);
        eventService.registerListener(connectionTask);
    }

    private void registerFrameworkCommands() {
        UtilCommands.registerCommands(
                serviceManager.provideUnchecked(ICommandService.class),
                serviceManager.provideUnchecked(IInjectionService.class)
        );
    }

    protected void loadPlugins() {

        // Load all plugins - This is where dependencies are being enforced
        Map<String, PluginRegistry> regMap = pMgr.getAllPlugins();
        regMap.forEach((n, pr) -> loadPlugin(regMap, n, pr));
        StringBuilder b = new StringBuilder();
        plugins.forEach((k, v) -> b.append(k).append(", "));
        logger.info("Loaded {} plugin(s): {}", plugins.size(), b);
        eventService.fireEvent(new BotStateEvent.PluginsLoaded().setBot(this));
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
            logger.error("Failed to construct plugin: {}", id, e);
            c.setState(PluginContainer.State.FAILED);
            return false;
        }

        injectionService.injectInto(c.getPlugin());
        eventService.registerListener(c.getPlugin());
        c.setState(PluginContainer.State.DONE);
        logger.debug("Initialized plugin {}", id);
        return true;
    }

    /**
     * Reads connection credentials and schedules the task used to connect to the TS3 server.
     */
    protected void scheduleConnect() {
        String host = config.getNode("host").asString();
        Integer port = config.getNode("port").asInteger();
        String user = config.getNode("user").asString();
        String pass = config.getNode("pass").asString();
        Integer ts3InstID = config.getNode("instance").asInteger();
        Boolean useSSL = config.getNode("ssl").optBoolean(false);
        String nickName = config.getNode("nick").optString("JeakBot");
        server.setCredentials(host, port, user, pass, ts3InstID, useSSL, nickName);

        Boolean doNetDump = Main.getProperty("bot.connection.netdump", Boolean.FALSE);
        if (doNetDump) {
            logger.info("Dedicated net-dumping is currently not implemented. The option will have no effect atm.");
        }

        serviceManager.provideUnchecked(ITaskService.class).runTask(connectionTask);
    }

    // * * * Configuration * * * //

    protected <S> S initializeService(S serviceInstance) {
        FrameworkService serviceInfo = serviceInstance.getClass().getAnnotation(FrameworkService.class);

        String className = serviceInstance.getClass().getName();
        if (serviceInfo == null) {
            logger.warn("Service is not annotated properly! Class: {}", className);
            return serviceInstance;
        }
        Class<?> serviceInterface = serviceInfo.serviceInterface();
        if (!serviceInterface.isAssignableFrom(serviceInstance.getClass())) {
            logger.error("Service interface not compatible with service class: {} vs. {}", serviceInterface.getName(), className);
            return serviceInstance;
        }

        //noinspection unchecked - Checked above.
        serviceManager.registerService(((Class<S>) serviceInterface), serviceInstance);
        injectionService.injectInto(serviceInstance);
        eventService.registerListener(serviceInstance);
        return serviceInstance;
    }

    /**
     * Initializes the bots configuration
     * Makes use of the {@link IBotStateEvent.IInitializeEvent} in order to cancel startup on unsuccessful init.
     */
    protected void initializeConfiguration(IBotStateEvent.IInitializeEvent event) {
        // Construct configLoader but only directly read from the file when it exists
        // Otherwise cancel startup and create default config

        try {
            configRep.load();

        } catch (FileNotFoundException e) {
            logger.warn("Creating new default configuration. Requesting shutdown after initialization.");
            configRep.createRoot();
            event.cancel();

        } catch (IOException | ParseException e) {
            logger.error("Failed to load configuration!", e);
            event.cancel();
            return;
        }
        config = configRep.getRoot();
        boolean rewrite = false;

        if (!config.getNode("host").isPrimitive()) {
            config.getNode("host").setString("localhost");
            rewrite = true;
        }

        if (!config.getNode("port").isPrimitive()) {
            config.getNode("port").setInteger(10011);
            rewrite = true;
        }

        if (!config.getNode("user").isPrimitive()) {
            config.getNode("user").setString("serveradmin");
            rewrite = true;
        }

        if (!config.getNode("pass").isPrimitive()) {
            config.getNode("pass").setString("password");
            rewrite = true;
        }

        if (!config.getNode("instance").isPrimitive()) {
            config.getNode("instance").setInteger(1);
            rewrite = true;
        }

        if (!config.getNode("nick").isPrimitive()) {
            config.getNode("nick").setString("JeakBot");
            rewrite = true;
        }

        if (rewrite) {
            if (!saveConfig()) {
                logger.error("Failed to rewrite configuration. Aborting startup, just in case.");
                event.cancel();
            }
            logger.warn("One or more settings have been set to default values. Please review the configuration.");
            event.cancel();
        }
    }

    public boolean saveConfig() {
        try {
            configRep.save();
            return true;
        } catch (IOException e) {
            logger.error("Failed to save configuration!", e);
            return false;
        }
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        logger.debug("Base directory changed to: {}", baseDir);
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
    public File getConfigDirectory() {
        return confDir;
    }

    @Override
    public IServer getServer() {
        return server;
    }

    // * * * RUNTIME * * * //

    public void shutdown() {

        // Decouple the shutdown callback from threads running inside the bots context.
        // This avoids any termination interrupts going on inside the framework instance from interrupting our shutdown handler.
        shutdownExecutor.execute(() -> {
            final BotStateEvent.PreShutdown preShutdown = new BotStateEvent.PreShutdown();
            preShutdown.setBot(this);
            eventService.fireEvent(preShutdown);
            var executors = new LinkedList<>(preShutdown.getExecutors());

            saveConfig();

            final BotStateEvent.PostShutdown postShutdown = new BotStateEvent.PostShutdown();
            postShutdown.setBot(this);
            eventService.fireEvent(postShutdown);
            executors.addAll(postShutdown.getExecutors());


            executors.removeIf(ExecutorService::isShutdown);
            if (!executors.isEmpty()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // ignore interruption.
                }
                executors.forEach(ExecutorService::shutdownNow);
            }

            eventService.shutdown();

            if (onShutdown != null) {
                onShutdown.accept(this);
            }
        });
    }

    public void onShutdown(Consumer<JeakBot> onShutdown) {
        this.onShutdown = onShutdown;
    }

    @Override
    public UUID getInstanceUUID() {
        return instanceUUID;
    }
}
