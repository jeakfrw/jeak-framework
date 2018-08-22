package de.fearnixx.t3.teamspeak.cache;

import de.fearnixx.t3.event.EventAbortException;
import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.QueryEvent;
import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.service.task.ITask;
import de.fearnixx.t3.task.TaskService;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.*;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.QueryConnection;
import de.mlessmann.logging.ILogReceiver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author MarkL4YG
 * Deployed work-arounds:
 * * enterview uses "ctid" instead of "cid"
 * * TS3 query sending invalid icon IDs. More info: https://twitter.com/MarkL4YG/status/965174407701385216
 */
public class DataCache implements IDataCache {

    private final Object LOCK = new Object();
    private ILogReceiver logger;

    private IQueryConnection connection;
    private IEventService eventService;

    private Map<Integer, TS3Client> clientCache;
    private Map<Integer, TS3Channel> channelCache;

    public DataCache(ILogReceiver logger, IQueryConnection connection, IEventService eventService) {
        this.logger = logger;
        this.connection = connection;
        this.eventService = eventService;
        clientCache = new ConcurrentHashMap<>(50);
        channelCache = new ConcurrentHashMap<>(60);
    }

    // == CLIENTLIST = //
    private final IQueryRequest clientListRequest = IQueryRequest.builder()
            .command("clientlist")
            .addOption("-uid")
            .addOption("-away")
            .addOption("-voice")
            .addOption("-times")
            .addOption("-groups")
            .addOption("-info")
            .addOption("-icon")
            .addOption("-country")
            .build();
    private final ITask clientListTask = ITask.builder()
            .name("t3server.clientRefresh")
            .interval(60L, TimeUnit.SECONDS)
            .runnable(() -> connection.sendRequest(clientListRequest, this::onQueryMessage))
            .build();

    // == CHANNELLIST == //
    private final IQueryRequest channelListRequest = IQueryRequest.builder()
            .command("channellist")
            .addOption("-topic")
            .addOption("-flags")
            .addOption("-voice")
            .addOption("-limits")
            .addOption("-icon")
            .build();
    private final ITask channelListTask = ITask.builder()
            .name("t3server.channelrefresh")
            .interval(3L, TimeUnit.MINUTES)
            .runnable(() -> connection.sendRequest(channelListRequest, this::onQueryMessage))
            .build();

    public void scheduleTasks(TaskService tm) {
        tm.runTask(clientListTask);
        tm.runTask(channelListTask);
    }


    public void reset() {
        synchronized (LOCK) {
            clientCache.forEach((clid, c) -> c.invalidate());
            clientCache.clear();
            channelCache.forEach((cid, c) -> c.invalidate());
            channelCache.clear();
        }
    }

    public Map<Integer, IClient> getClientMap() {
        synchronized (LOCK) {
            return Collections.unmodifiableMap(clientCache);
        }
    }

    public Map<Integer, IChannel> getChannelMap() {
        synchronized (LOCK) {
            return Collections.unmodifiableMap(channelCache);
        }
    }

    public List<IClient> getClients() {
        synchronized (LOCK) {
            return Collections.unmodifiableList(new ArrayList<>(clientCache.values()));
        }
    }

    public List<IChannel> getChannels() {
        synchronized (LOCK) {
            return Collections.unmodifiableList(new ArrayList<>(channelCache.values()));
        }
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onQueryNotification(IQueryEvent.INotification event) {

        if (event instanceof QueryEvent.Notification.TargetClient) {
            processTargetClient(((QueryEvent.TargetClient) event));

        } else if (event instanceof QueryEvent.Notification.TargetChannel) {
            processTargetChannel(((QueryEvent.TargetChannel) event));
        }
    }

    private void processTargetClient(QueryEvent.Notification.TargetClient event) {
        if (event instanceof QueryEvent.Notification.TargetClient.ClientEnter) {
            TS3Client client = new TS3Client();
            client.copyFrom(event);
            client.setProperty("cid", client.getProperty("ctid").orElse(null));
            clientCache.put(client.getClientID(), client);
        }

        String targetPropertyName = PropertyKeys.Client.ID;
        if (event instanceof IQueryEvent.INotification.ITextMessage)
            targetPropertyName = PropertyKeys.TextMessage.SOURCE_ID;

        Optional<String> optClientID = event.getProperty(targetPropertyName);
        if (optClientID.isPresent()) {
            Integer clientID = Integer.valueOf(optClientID.get());
            TS3Client client = clientCache.getOrDefault(clientID, null);

            if (client != null) {
                event.setClient(client);
                return;
            }
        }
        throw new EventAbortException("Target/Client injection failed! Event aborted! " + optClientID.orElse("null"));
    }

    private void processTargetChannel(QueryEvent.Notification.TargetChannel event) {
        if (event instanceof QueryEvent.Notification.TargetChannel.ChannelCreate) {
            String optName = event.getProperty("channel_name").get();
            boolean isSpacer = TS3Spacer.spacerPattern.matcher(optName).matches();
            TS3Channel channel = isSpacer ? new TS3Spacer() : new TS3Channel();
            channel.copyFrom(event);
            channelCache.put(channel.getID(), channel);
        }

        Optional<String> optChannelID = event.getProperty("cid");
        if (optChannelID.isPresent()) {
            Integer channelID = Integer.valueOf(optChannelID.get());
            TS3Channel client = channelCache.getOrDefault(channelID, null);

            if (client != null) {
                event.setChannel(client);
                return;
            }
        }
        throw new EventAbortException("Target/Channel injection failed! Event aborted!");
    }

    /**
     * Generic listeners to update caches.
     * This HAVE TO run after normal event processing!
     */
    @Listener(order = Listener.Orders.LATEST)
    public void onNotify(IQueryEvent.INotification event) {
        if (event instanceof IQueryEvent.INotification.ITargetClient.IClientMoved) {
            // Client has moved - Apply to representation
            synchronized (LOCK) {
                Integer clientID = Integer.valueOf(event.getProperty("clid").orElse("-1"));
                TS3Client client = clientCache.getOrDefault(clientID, null);
                if (client == null) {
                    logger.warning("Insufficient information for clientMoved update: Client not yet cached.");
                    return;
                }

                TS3Channel fromChannel = channelCache.getOrDefault(client.getChannelID(), null);
                Integer fromChannelId = fromChannel != null ? fromChannel.getID() : -1;
                TS3Channel toChannel = channelCache.getOrDefault(Integer.parseInt(event.getProperty("ctid").get()), null);
                Integer toChannelId = toChannel != null ? toChannel.getID() : -1;
                if (fromChannel == null || toChannel == null) {
                    logger.warning("Insufficient information for clientMoved update: ", fromChannelId, "->", toChannelId);
                    return;
                }

                logger.fine("Updating cached client ", clientID, " \"", PropertyKeys.Client.CHANNEL_ID, "\" ",
                        fromChannelId, "->", toChannelId);

                // Set new channel
                client.setProperty(PropertyKeys.Client.CHANNEL_ID, fromChannelId.toString());
                // Set new client count - FROM
                fromChannel.setProperty(
                        PropertyKeys.Channel.CLIENT_COUNT,
                        Integer.toString(fromChannel.getClientCount() - 1));
                fromChannel.setProperty(
                        PropertyKeys.Channel.CLIENT_COUNT_FAMILY,
                        Integer.toString(fromChannel.getClientCount() - 1));
                // Set new client count - TO
                toChannel.setProperty(
                        PropertyKeys.Channel.CLIENT_COUNT,
                        Integer.toString(toChannel.getClientCount() + 1));
                toChannel.setProperty(
                        PropertyKeys.Channel.CLIENT_COUNT_FAMILY,
                        Integer.toString(toChannel.getClientCount() + 1));
            }

        } else if (event instanceof IQueryEvent.INotification.IClientLeave) {
            // Client has left - Apply to representation
            synchronized (LOCK) {
                Integer clientID = Integer.parseInt(event.getProperty("clid").get());
                TS3Client client = clientCache.getOrDefault(clientID, null);
                if (client == null)
                    return;
                client.invalidate();
                clientCache.remove(clientID);
            }
        }
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onQueryMessage(IRawQueryEvent.IMessage.IAnswer event) {
        if (event.getError().getCode() != 0)
            return;

        if (event.getRequest() == clientListRequest) {
            refreshClients(event);

        } else if (event.getRequest() == channelListRequest) {
            refreshChannels(event);
        }
    }

    /**
     * Refreshes the internal client cache based off a `clientlist` answer event.
     * All methods and listeners assume that all options were set during the request.
     */
    private void refreshClients(IRawQueryEvent.IMessage.IAnswer event) {
        List<IRawQueryEvent.IMessage> objects = event.toList();
        synchronized (LOCK) {
            final Map<Integer, TS3Client> clientMapping = generateClientMapping(objects);

            TS3Client oldClientRep;
            Integer oID;
            TS3Client freshClient;

            Integer[] cIDs = clientCache.keySet().toArray(new Integer[0]);
            for (int i = clientCache.size() - 1; i >= 0; i--) {
                oID = cIDs[i];
                oldClientRep = clientCache.get(oID);
                freshClient = clientMapping.getOrDefault(oID, null);

                if (freshClient == null) {
                    // Client removed - invalidate & remove
                    oldClientRep.invalidate();
                    channelCache.remove(oID);

                } else {
                    clientMapping.remove(oID);
                }
            }

            // All others are new - Add them
            clientMapping.forEach(clientCache::put);
            clientMapping.clear();
        }

        logger.finer("Clientlist updated");
        QueryEvent refresh = new QueryEvent.BasicDataEvent.RefreshClients();
        refresh.setConnection(((QueryConnection) event.getConnection()));
        eventService.fireEvent(refresh);
    }

    /**
     * Creates a Map of all available clients from a `clientlist` answer event.
     * Helper method for {@link #refreshClients(IRawQueryEvent.IMessage.IAnswer)}.
     *
     * Handles update existing and creating clients
     */
    private Map<Integer, TS3Client> generateClientMapping(List<IRawQueryEvent.IMessage> messageObjects) {
        final Map<Integer, TS3Client> mapping = new ConcurrentHashMap<>(messageObjects.size(), 1.1f);
        messageObjects
                .stream()
                .parallel()
                .forEach(message -> {
                    try {
                        int cid = Integer.parseInt(message.getProperty(PropertyKeys.Client.ID).orElse("-1"));

                        if (cid == -1) {
                            logger.warning("Skipping a client due to invalid ID: ",
                                    message.getProperty(PropertyKeys.Client.ID)
                                            .orElse("null"));
                            return;
                        }

                        TS3Client client = clientCache.getOrDefault(cid, null);

                        if (client == null) {
                            // Client is new - New reference
                            client = new TS3Client();
                            client.copyFrom(message);

                        } else {
                            // Client not new - Update values
                            client.copyFrom(message);
                        }

                        mapping.put(cid, client);
                    } catch (Exception e) {
                        logger.warning("Failed to parse a client", e);
                    }
                });
        return mapping;
    }

    /**
     * Refreshes the internal client cache based off a `channellist` answer event.
     * All methods and listeners assume that all options were set during the request.
     */
    private void refreshChannels(IRawQueryEvent.IMessage.IAnswer event) {
        List<IRawQueryEvent.IMessage> messages = event.toList();
        synchronized (LOCK) {
            final Map<Integer, TS3Channel> newMap = generateChannelMapping(messages);
            TS3Channel o;
            Integer oID;
            TS3Channel n;
            Integer[] cIDs = channelCache.keySet().toArray(new Integer[0]);
            for (int i = channelCache.size() - 1; i >= 0; i--) {
                oID = cIDs[i];
                o = channelCache.get(oID);
                o.clearChildren();
                n = newMap.getOrDefault(oID, null);
                if (n == null) {
                    // Channel removed - invalidate & remove
                    o.invalidate();
                    channelCache.remove(oID);

                } else if (n == o) {
                    // Channel unchanged - continue
                    newMap.remove(oID);

                } else {
                    // Channel reference updated - invalidate & change
                    o.invalidate();
                    channelCache.put(oID, n);
                    newMap.remove(oID);
                }
            }

            // All others are new - Add them
            newMap.forEach(channelCache::put);
            channelCache.forEach((cid, c) -> {
                int pid = c.getParent();
                if (pid == 0) return;
                TS3Channel parent = channelCache.getOrDefault(pid, null);
                if (parent == null) {
                    logger.warning("Channel", cid, "has nonexistent parent", c.getParent());
                    return;
                }
                parent.addSubChannel(c);
            });
        }

        logger.finer("Channellist updated");
        QueryEvent refresh = new QueryEvent.BasicDataEvent.RefreshChannels();
        refresh.setConnection(((QueryConnection) event.getConnection()));
        eventService.fireEvent(refresh);
    }

    /**
     * Creates a Map of all available clients from a `channellist` answer event.
     * Helper method for {@link #refreshChannels(IRawQueryEvent.IMessage.IAnswer)}.
     *
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
                    logger.warning("Skipping a channel due to invalid channel ID");
                    return;
                }

                TS3Channel channel = channelCache.getOrDefault(cid, null);

                if (channel == null) {
                    // Channel is new - New reference
                    channel = new TS3Channel();
                    channel.copyFrom(o);

                } else {
                    String nName = o.getProperty(PropertyKeys.Channel.NAME).orElse(null);

                    if (nName == null) {
                        logger.warning("Skipping a channel due to missing name");
                        return;
                    }

                    boolean wasSpacer = channel.isSpacer();
                    boolean isSpacer = TS3Spacer.spacerPattern.matcher(nName).matches();

                    if (isSpacer != wasSpacer) {
                        // Spacer state changed - Update reference
                        channel = isSpacer ? new TS3Spacer() : new TS3Channel();
                        channel.copyFrom(o);

                    } else {
                        // Channel not new - Update values
                        channel.copyFrom(o);
                    }
                }

                fixChannelIconId(channel);
                channelMap.put(cid, channel);
            } catch (Exception e) {
                logger.warning("Failed to parse a channel", e);
            }
        });
        return channelMap;
    }

    /**
     * Dirtiest work-around I've ever committed...
     * TeamSpeak appears to read their integers wrongly and sends back an invalid ID.
     *
     * If the channel icon id is negative it has erroneously been read as signed integer.
     */
    private void fixChannelIconId(TS3Channel channel) {
        Optional<String> optIconId = channel.getProperty("channel_icon_id");
        if (optIconId.isPresent()) {
            Integer idFromTS = Integer.valueOf(optIconId.get());

            if (idFromTS < 0) {
                long realID = Integer.toUnsignedLong(idFromTS);
                channel.setProperty(
                        PropertyKeys.Channel.ICON_ID,
                        Long.toString(realID)
                );
            }
        }
    }
}