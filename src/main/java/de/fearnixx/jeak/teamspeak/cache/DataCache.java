package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private final GenericInfoCache genericInfoCache = new GenericInfoCache();

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

        injectionService.injectInto(genericInfoCache);
        eventService.registerListener(genericInfoCache);
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

    @Override
    public Optional<IDataHolder> getServerInfo() {
        return genericInfoCache.getServerInfo();
    }

    @Override
    public Optional<IDataHolder> getInstanceInfo() {
        return genericInfoCache.getInstanceInfo();
    }
}