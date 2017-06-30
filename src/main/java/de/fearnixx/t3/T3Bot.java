package de.fearnixx.t3;

import de.fearnixx.t3.event.EventManager;
import de.fearnixx.t3.event.state.BotStateEvent;
import de.fearnixx.t3.reflect.annotation.Inject;
import de.fearnixx.t3.reflect.plugins.PluginContainer;
import de.fearnixx.t3.reflect.plugins.persistent.PluginManager;
import de.fearnixx.t3.reflect.plugins.persistent.PluginRegistry;
import de.fearnixx.t3.service.IServiceManager;
import de.fearnixx.t3.service.ServiceManager;
import de.fearnixx.t3.service.db.DBReader;
import de.fearnixx.t3.service.db.IDBReader;
import de.fearnixx.t3.task.ITaskManager;
import de.fearnixx.t3.task.TaskManager;
import de.fearnixx.t3.ts3.ITS3Server;
import de.fearnixx.t3.ts3.TS3Server;
import de.fearnixx.t3.ts3.query.QueryConnectException;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class T3Bot implements Runnable, IT3Bot {

    // * * * STATICS  * * * //
    public static final Charset CHAR_ENCODING = Charset.forName("UTF-8");
    public static final String VERSION = "@VERSION@";

    // * * * VOLATILES * * * //

    private volatile boolean initCalled = false;

    // * * * FIELDS * * * //

    private ILogReceiver log;
    private File confFile;
    private ConfigLoader loader;

    private ConfigNode config;

    private PluginManager pMgr;
    private Map<String, PluginContainer> plugins;

    private TS3Server server;

    private EventManager eventManager;
    private TaskManager taskManager;
    private ServiceManager serviceManager;
    private DBReader dbReader;

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
            throw new RuntimeException("Reinitialization of T3Bot instances is not supported! Completely shut down the instance beforehand and/or create a new one.");
        }
        initCalled = true;
        loader = new JSONConfigLoader();
        loader.setEncoding(CHAR_ENCODING);
        config = loader.loadFromFile(confFile);
        if (loader.hasError()) {
            throw new RuntimeException("Can't read configuration! " + confFile.getPath(), loader.getError());
        }

        boolean rewrite = false;

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
        }
        if (!config.getNode("instance").isType(Integer.class)) {
            config.getNode("instance").setValue(1);
            rewrite = true;
        }

        if (rewrite) {
            saveConfig();
        }
        plugins = new HashMap<>();
        eventManager = new EventManager(log.getChild("EM"));
        serviceManager = new ServiceManager();
        taskManager = new TaskManager(log.getChild("TM"), (pMgr.estimateCount() > 0 ? pMgr.estimateCount() : 10) * 10);
        server = new TS3Server(eventManager, log.getChild("SVR"));
        dbReader = new DBReader(log.getChild("DBR"), server);

        taskManager.start();

        pMgr.load(true);
        Map<String, PluginRegistry> regMap = pMgr.getAllPlugins();
        // Load all plugins - This is where dependencies are being enforced
        regMap.forEach((n, pr) -> loadPlugin(regMap, n, pr));
        log.info(plugins.size(), "plugins loaded");
        eventManager.fireEvent(new BotStateEvent.PluginsLoaded(this));

        String host = config.getNode("host").getString();
        Integer port = config.getNode("port").getInt();
        String user = config.getNode("user").getString();
        String pass = config.getNode("pass").getString();
        Integer instID = config.getNode("instance").getInt();
        eventManager.fireEvent(new BotStateEvent.PreConnect(this));
        try {
            server.connect(host, port, user, pass, instID);
        } catch (QueryConnectException e) {
            log.severe("Failed to start bot: TS3INIT failed", e);
            shutdown();
            return;
        }
        server.scheduleTasks(taskManager);
        log.info("Connected");
        eventManager.fireEvent(new BotStateEvent.PostConnect(this));
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
        final Object p = c.getPlugin();
        try {
            boolean a = false;
            // Logging
            log.finer("Injecting logReceivers");
            for (Field f : c.getInjectionsFor(ILogReceiver.class)) {
                log.finest("Injecting field ", f.getName());
                Inject i = f.getAnnotation(Inject.class);
                a = f.isAccessible();
                f.setAccessible(true);
                if (i.id().isEmpty()) {
                    f.set(p, this.log.getChild(id));
                } else {
                    f.set(p, this.log.getChild(i.id()));
                }
                f.setAccessible(a);
            }
            // Configuration
            log.finer("Injecting configs");
            boolean confInjected = false;
            for (Field f : c.getInjectionsFor(ConfigLoader.class)) {
                log.finest("Injecting field ", f.getName());
                Inject i = f.getAnnotation(Inject.class);
                a = f.isAccessible();
                f.setAccessible(true);
                if (i.id().isEmpty()) {
                    if (confInjected) {
                        throw new IllegalStateException("Configuration has already been injected! Use custom IDs for multiple configs!");
                    } else {
                        File cf = new File(getDir(), "config/" + id + ".json");
                        ConfigLoader cl = new JSONConfigLoader();
                        cl.setEncoding(CHAR_ENCODING);
                        cl.setFile(cf);
                        f.set(p, cl);
                        confInjected = true;
                    }
                } else {
                    File cf = new File(getDir(), "config/" + id + "/" + i.id() + ".json");
                    cf.getAbsoluteFile().getParentFile().mkdirs();
                    ConfigLoader cl = new JSONConfigLoader();
                    cl.setEncoding(CHAR_ENCODING);
                    cl.setFile(cf);
                    f.set(p, cl);
                    confInjected = true;
                }
            }
            // Bot
            log.finer("Injecting bot");
            for (Field f : c.getInjectionsFor(IT3Bot.class)) {
                log.finest("Injecting field ", f.getName());
                Inject i = f.getAnnotation(Inject.class);
                a = f.isAccessible();
                f.setAccessible(true);
                if (i.id().isEmpty()) {
                    f.set(p, this);
                } else {
                    f.set(p, this);
                }
                f.setAccessible(a);
            }
        } catch (Exception e) {
            log.severe("Failed to run injections for: ", id, e);
            c.setState(PluginContainer.State.FAILED);
            return false;
        }
        eventManager.addContainer(c.getListener());
        c.setState(PluginContainer.State.DONE);
        log.fine("Initialized plugin ", id);
        return true;
    }

    // * * * Configuration * * * //

    public void saveConfig() {
        loader.resetError();
        loader.save(config);
        if (loader.hasError()) {
            log.severe("Failed to save configuration: ", loader.getError().getMessage(), loader.getError());
        }
    }

    // * * * MISC * * * //

    @Override
    public File getDir() {
        return confFile.getAbsoluteFile().getParentFile().getParentFile();
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public ITaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public IServiceManager getServiceManager() { return serviceManager; }

    @Override
    public ITS3Server getServer() {
        return server;
    }

    public IDBReader getDBReader() {
        return dbReader;
    }

    // * * * RUNTIME * * * //

    public void shutdown() {
        eventManager.fireEvent(new BotStateEvent.PreShutdown(this));
        saveConfig();
        taskManager.shutdown();
        dbReader.shutdown();
        server.shutdown();
        eventManager.fireEvent(new BotStateEvent.PostShutdown(this));
    }
}
