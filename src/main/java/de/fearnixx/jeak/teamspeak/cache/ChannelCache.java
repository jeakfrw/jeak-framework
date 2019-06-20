package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.teamspeak.TS3PermissionSubject;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.TS3Channel;
import de.fearnixx.jeak.teamspeak.data.TS3ChannelHolder;
import de.fearnixx.jeak.teamspeak.data.TS3Spacer;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.util.TS3DataFixes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChannelCache {

    private static final Logger logger = LoggerFactory.getLogger(ChannelCache.class);

    private final Map<Integer, TS3Channel> internalCache = new ConcurrentHashMap<>(60);

    @Inject
    private IEventService eventService;

    @Inject
    private ITaskService taskService;

    @Inject
    private IServer server;

    @Inject
    private IPermissionService permService;

    private final Object LOCK;

    // == CHANNELLIST == //
    private final IQueryRequest channelListRequest = IQueryRequest.builder()
            .command(QueryCommands.CHANNEL.CHANNEL_LIST)
            .addOption("-topic")
            .addOption("-flags")
            .addOption("-voice")
            .addOption("-limits")
            .addOption("-icon")
            .onDone(this::onListAnswer)
            .build();
    private final ITask channelListTask = ITask.builder()
            .name("cache.channelrefresh")
            .interval(DataCache.CHANNEL_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .runnable(() -> server.optConnection().ifPresent(conn -> conn.sendRequest(channelListRequest)))
            .build();

    public ChannelCache(Object lock) {
        this.LOCK = lock;
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        taskService.runTask(channelListTask);
    }

    @Listener(order = Listener.Orders.LATEST)
    public void onDisconnected(IBotStateEvent.IConnectStateEvent.IDisconnect event) {
        synchronized (LOCK) {
            logger.info("Clearing channel cache due to disconnect.");
            taskService.removeTask(channelListTask);
            internalCache.values().forEach(TS3ChannelHolder::invalidate);
            internalCache.clear();
        }
    }

    private void onListAnswer(IQueryEvent.IAnswer event) {
        if (event.getRequest() == channelListRequest) {
            if (event.getErrorCode() == 0) {
                refreshChannels((IRawQueryEvent.IMessage.IAnswer) event.getRawReference());
            } else {
                logger.warn("Channel-List data refresh returned an error: {} - {}", event.getErrorCode(), event.getErrorMessage());
            }
        }
    }

    /**
     * Refreshes the internal client cache based off a `channellist` answer event.
     * All methods and listeners assume that all options were set during the request.
     */
    private void refreshChannels(IRawQueryEvent.IMessage.IAnswer event) {
        final List<IRawQueryEvent.IMessage> messages = event.toList();
        synchronized (internalCache) {
            final Map<Integer, TS3Channel> newMap = generateChannelMapping(messages);
            TS3Channel o;
            Integer oID;
            TS3Channel n;
            Integer[] cIDs = internalCache.keySet().toArray(new Integer[0]);
            for (int i = internalCache.size() - 1; i >= 0; i--) {
                oID = cIDs[i];
                o = internalCache.get(oID);
                o.clearChildren();
                n = newMap.getOrDefault(oID, null);
                if (n == null) {
                    // Channel removed - invalidate & remove
                    o.invalidate();
                    internalCache.remove(oID);

                } else if (n == o) {
                    // Channel unchanged - continue
                    newMap.remove(oID);

                } else {
                    // Channel reference updated - invalidate & change
                    o.invalidate();
                    internalCache.put(oID, n);
                    newMap.remove(oID);
                }
            }

            // All others are new - Add them
            newMap.forEach(internalCache::put);

            // Update children
            internalCache.forEach((cid, c) -> {
                int pid = c.getParent();
                if (pid == 0) return;
                TS3Channel parent = internalCache.getOrDefault(pid, null);
                if (parent == null) {
                    logger.warn("Channel {} has nonexistent parent: {}", cid, c.getParent());
                    return;
                }
                parent.addSubChannel(c);
            });

            // Ensure children lists to be ordered
            // TODO: Find a better solution than re-sorting the whole list every time!
            internalCache.values().forEach(TS3Channel::sortChildren);
        }

        logger.debug("Channellist updated");
        QueryEvent refresh = new QueryEvent.BasicDataEvent
                .RefreshChannels(getChannels(), getChannelMap());
        refresh.setConnection(event.getConnection());
        refresh.setRawReference(event);
        eventService.fireEvent(refresh);
    }

    /**
     * Creates a Map of all available clients from a `channellist` answer event.
     * Helper method for {@link #refreshChannels(IRawQueryEvent.IMessage.IAnswer)}.
     * <p>
     * Handles update existing and creating channels.
     */
    private Map<Integer, TS3Channel> generateChannelMapping(List<IRawQueryEvent.IMessage> messageObjects) {
        final Map<Integer, TS3Channel> channelMap = new ConcurrentHashMap<>(messageObjects.size(), 1.1f);
        messageObjects
                .stream()
                .parallel()
                .forEach(o -> {
                    try {
                        int cid = Integer.parseInt(o.getProperty(PropertyKeys.Channel.ID).orElse("-1"));
                        if (cid == -1) {
                            logger.warn("Skipping a channel due to invalid channel ID");
                            return;
                        }

                        TS3Channel channel = internalCache.getOrDefault(cid, null);

                        if (channel == null) {
                            // Channel is new - New reference
                            channel = new TS3Channel();
                            channel.copyFrom(o);

                        } else {
                            String nName = o.getProperty(PropertyKeys.Channel.NAME).orElse(null);

                            if (nName == null) {
                                logger.warn("Skipping a channel due to missing name");
                                return;
                            }

                            boolean wasSpacer = channel.isSpacer();
                            boolean isSpacer = TS3Spacer.spacerPattern.matcher(nName).matches();

                            if (isSpacer != wasSpacer) {
                                // Spacer state changed - Update reference
                                channel = createChannel(isSpacer);
                                channel.copyFrom(o);

                            } else {
                                // Channel not new - Update values
                                channel.copyFrom(o);
                            }
                        }

                        // Fix channel icon ID in case it got misread by TS3
                        TS3DataFixes.ICONS_INVALID_CRC32(channel, PropertyKeys.Channel.ICON_ID);
                        channelMap.put(cid, channel);
                    } catch (Exception e) {
                        logger.warn("Failed to parse a channel", e);
                    }
                });
        return channelMap;
    }

    private TS3Channel createChannel(boolean isSpacer) {
        TS3Channel channel = isSpacer ? new TS3Spacer() : new TS3Channel();
        channel.setPermSubject(new TS3PermissionSubject(UUID.randomUUID()));
        return channel;
    }

    public List<IChannel> getChannels() {
        synchronized (internalCache) {
            return List.copyOf(internalCache.values());
        }
    }

    public Map<Integer, IChannel> getChannelMap() {
        synchronized (internalCache) {
            return Collections.unmodifiableMap(internalCache);
        }
    }

    Map<Integer, TS3Channel> getUnsafeChannelMap() {
        synchronized (internalCache) {
            return internalCache;
        }
    }
}
