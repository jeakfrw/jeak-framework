package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.EventAbortException;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.task.TaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.*;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.util.TS3DataFixes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@FrameworkService(serviceInterface = IDataCache.class)
public class DataCache implements IDataCache {

    public static final int CLIENT_REFRESH_INTERVAL = Main.getProperty("jeak.cache.clientRefresh", 60);
    public static final int CHANNEL_REFRESH_INTERVAL = Main.getProperty("jeak.cache.channelRefresh", 180);
    private static final Logger logger = LoggerFactory.getLogger(DataCache.class);

    @Inject
    private IEventService eventService;

    @Inject
    private IInjectionService injectionService;

    private final Object LOCK = new Object();
    private final ChannelCache channelCache = new ChannelCache(LOCK);
    private final ClientCache clientCache = new ClientCache(LOCK);
    private final ChannelUpdateWatcher channelUpdateWatcher = new ChannelUpdateWatcher(LOCK, this);
    private final ClientUpdateWatcher clientUpdateWatcher = new ClientUpdateWatcher(LOCK, this);
    private final EventDataInjector dataInjector = new EventDataInjector(LOCK, this);

    @Listener(order = Listener.Orders.SYSTEM)
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        injectionService.injectInto(channelCache);
        eventService.registerListener(channelCache);
        injectionService.injectInto(clientCache);
        eventService.registerListener(clientCache);

        injectionService.injectInto(channelUpdateWatcher);
        eventService.registerListener(channelUpdateWatcher);
        injectionService.injectInto(clientUpdateWatcher);
        eventService.registerListener(clientUpdateWatcher);

        injectionService.injectInto(dataInjector);
        eventService.registerListener(dataInjector);
    }

    @Override
    public Optional<IClient> findClientByUniqueId(String uniqueId) {
        return getClients().stream()
                .filter(c -> c.getClientUniqueID().equals(uniqueId))
                .findFirst();
    }

    @Override
    public Optional<IChannel> findChannelByName(String name) {
        return getChannels()
                .stream()
                .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                .findFirst();
    }

    @Override
    public int getClientRefreshTime() {
        return CLIENT_REFRESH_INTERVAL;
    }

    @Override
    public int getChannelRefreshTime() {
        return CHANNEL_REFRESH_INTERVAL;
    }

    @Override
    public Map<Integer, IClient> getClientMap() {
        return clientCache.getClientMap();
    }

    @Override
    public Map<Integer, IChannel> getChannelMap() {
        return channelCache.getChannelMap();
    }

    @Override
    public List<IClient> getClients() {
        return clientCache.getClients();
    }

    @Override
    public List<IChannel> getChannels() {
        return channelCache.getChannels();
    }

    Map<Integer, TS3Channel> unsafeGetChannels() {
        return channelCache.getUnsafeChannelMap();
    }

    Map<Integer, TS3Client> unsafeGetClients() {
        return clientCache.getUnsafeClientMap();
    }
}