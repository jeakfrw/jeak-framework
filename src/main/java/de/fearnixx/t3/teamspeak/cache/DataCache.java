package de.fearnixx.t3.teamspeak.cache;

import de.fearnixx.t3.event.EventAbortException;
import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.QueryEvent;
import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.reflect.SystemListener;
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
 *
 * Deployed work-arounds:
 * * enterview uses "ctid" instead of "cid"
 * * TS3 query sending invalid icon IDs. More info: https://twitter.com/MarkL4YG/status/965174407701385216
 */
public class DataCache implements IDataCache {

    private final Object lock = new Object();
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
        synchronized (lock) {
            clientCache.forEach((clid, c) -> c.invalidate());
            clientCache.clear();
            channelCache.forEach((cid, c) -> c.invalidate());
            channelCache.clear();
        }
    }

    public Map<Integer, IClient> getClientMap() {
        synchronized (lock) {
            return new HashMap<>(clientCache);
        }
    }

    public Map<Integer, IChannel> getChannelMap() {
        synchronized (lock) {
            return new HashMap<>(channelCache);
        }
    }

    public List<IClient> getClients() {
        synchronized (lock) {
            return new ArrayList<>(clientCache.values());
        }
    }

    public List<IChannel> getChannels() {
        synchronized (lock) {
            return new ArrayList<>(channelCache.values());
        }
    }

    @SystemListener
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

    @Listener
    public void onNotify(IQueryEvent.INotification event) {
        if (event instanceof IQueryEvent.INotification.ITargetClient.IClientMoved) {
            // Client has moved - Apply to representation
            synchronized (lock) {
                IClient iClient = ((IQueryEvent.INotification.IClientMoved) event).getTarget();
                Integer clientID = iClient.getClientID();
                TS3Client client = clientCache.getOrDefault(clientID, null);
                if (client == null)
                    return;

                TS3Channel fromChannel = channelCache.getOrDefault(client.getChannelID(), null);
                TS3Channel toChannel = channelCache.getOrDefault(Integer.parseInt(event.getProperty("ctid").get()), null);
                if (fromChannel == null || toChannel == null || fromChannel == toChannel)
                    return;

                // Set new channel
                client.setProperty(PropertyKeys.Client.CHANNEL_ID, toChannel.getID().toString());
                // Set new client count - FROM
                fromChannel.setProperty(
                PropertyKeys.Channel.CLIENT_COUNT,
                Integer.valueOf(fromChannel.getClientCount() - 1).toString());
                fromChannel.setProperty(
                PropertyKeys.Channel.CLIENT_COUNT_FAMILY,
                Integer.valueOf(fromChannel.getClientCount() - 1).toString());
                // Set new client count - TO
                toChannel.setProperty(
                PropertyKeys.Channel.CLIENT_COUNT,
                Integer.valueOf(toChannel.getClientCount() + 1).toString());
                toChannel.setProperty(
                PropertyKeys.Channel.CLIENT_COUNT_FAMILY,
                Integer.valueOf(toChannel.getClientCount() + 1).toString());
            }

        } else if (event instanceof IQueryEvent.INotification.IClientLeave) {
            // Client has left - Apply to representation
            synchronized (lock) {
                Integer clientID = Integer.parseInt(event.getProperty("clid").get());
                TS3Client client = clientCache.getOrDefault(clientID, null);
                if (client == null)
                    return;
                client.invalidate();
                clientCache.remove(clientID);
            }
        }
    }


    public void onQueryMessage(IRawQueryEvent.IMessage.IAnswer event) {
        if (event.getError().getCode() != 0)
            return;
        if (event.getRequest() == clientListRequest) {
            List<IRawQueryEvent.IMessage> objects = event.toList();
            // Just a lock to be used when the new or old mapping is accessed
            final Object tempLock = new Object();
            final Map<Integer, TS3Client> newMap = new HashMap<>(objects.size(), 1.1f);
            synchronized (lock) {
                objects.stream().forEach(message -> {
                    try {
                        Integer cid = Integer.parseInt(message.getProperty(PropertyKeys.Client.ID).orElse("-1"));
                        if (cid == -1) {
                            logger.warning("Skipping a client due to invalid ID: ", message.getProperty(PropertyKeys.Client.ID).orElse("null"));
                            return;
                        }
                        TS3Client c;
                        synchronized (tempLock) {
                            c = clientCache.getOrDefault(cid, null);
                        }
                        if (c == null) {
                            // Client is new - New reference
                            c = new TS3Client();
                            c.copyFrom(message);
                        } else {
                            // Client not new - Update values
                            c.copyFrom(message);
                        }
                        synchronized (tempLock) {
                            newMap.put(cid, c);
                        }
                    } catch (Exception e) {
                        logger.warning("Failed to parse a client", e);
                    }
                });
                TS3Client o;
                Integer oID;
                TS3Client n;
                Integer[] cIDs = clientCache.keySet().toArray(new Integer[clientCache.keySet().size()]);
                for (int i = clientCache.size() - 1; i >= 0; i--) {
                    oID = cIDs[i];
                    o = clientCache.get(oID);
                    n = newMap.getOrDefault(oID, null);
                    if (n == null) {
                        // Client removed - invalidate & remove
                        o.invalidate();
                        channelCache.remove(oID);
                        continue;
                    } else {
                        newMap.remove(oID);
                        continue;
                    }
                }
                // All others are new - Add them
                newMap.forEach(clientCache::put);
                newMap.clear();
            }

            logger.finer("Clientlist updated");
            QueryEvent refresh = new QueryEvent.BasicDataEvent.RefreshClients();
            refresh.setConnection(((QueryConnection) event.getConnection()));
            eventService.fireEvent(refresh);

        } else if (event.getRequest() == channelListRequest) {

            List<IRawQueryEvent.IMessage> messages = event.toList();
            // Just a lock to be used when the new or old mapping is accessed
            final Object tempLock = new Object();
            final Map<Integer, TS3Channel> newMap = new HashMap<>(messages.size(), 1.1f);
            synchronized (lock) {
                messages.parallelStream().forEach(o -> {
                    try {
                        Integer cid = Integer.parseInt(o.getProperty(PropertyKeys.Channel.ID).orElse("-1"));
                        if (cid == -1) {
                            logger.warning("Skipping a channel due to invalid channel ID");
                            return;
                        }
                        TS3Channel c;
                        synchronized (tempLock) {
                            c = channelCache.getOrDefault(cid, null);
                        }
                        if (c == null) {
                            // Channel is new - New reference
                            c = new TS3Channel();
                            c.copyFrom(o);
                        } else {
                            String nName = o.getProperty(PropertyKeys.Channel.NAME).orElse(null);
                            if (nName == null) {
                                logger.warning("Skipping a channel due to missing name");
                                return;
                            }
                            boolean wasSpacer = c.isSpacer();
                            boolean isSpacer = TS3Spacer.spacerPattern.matcher(nName).matches();
                            if (isSpacer != wasSpacer) {
                                // Spacer state changed - Update reference
                                if (isSpacer)
                                    c = new TS3Spacer();
                                else
                                    c = new TS3Channel();
                                c.copyFrom(o);
                            } else {
                                // Channel not new - Update values
                                c.copyFrom(o);
                            }
                        }
                        // Dirtiest work-around I've ever committed...
                        if (c.getProperty("channel_icon_id").isPresent()) {
                            Integer idFromTS = Integer.valueOf(c.getProperty("channel_icon_id").get());
                            if (idFromTS < 0) {
                                Long realID = Integer.toUnsignedLong(idFromTS);
                                c.setProperty("channel_icon_id", realID.toString());
                            }
                        }
                        synchronized (tempLock) {
                            newMap.put(cid, c);
                        }
                    } catch (Exception e) {
                        logger.warning("Failed to parse a channel", e);
                    }
                });
                TS3Channel o;
                Integer oID;
                TS3Channel n;
                Integer[] cIDs = channelCache.keySet().toArray(new Integer[channelCache.keySet().size()]);
                for (int i = channelCache.size() - 1; i >= 0; i--) {
                    oID = cIDs[i];
                    o = channelCache.get(oID);
                    o.clearChildren();
                    n = newMap.getOrDefault(oID, null);
                    if (n == null) {
                        // Channel removed - invalidate & remove
                        o.invalidate();
                        channelCache.remove(oID);
                        continue;
                    } else if (n == o) {
                        // Channel unchanged - continue
                        newMap.remove(oID);
                        continue;
                    } else {
                        // Channel reference updated - invalidate & change
                        o.invalidate();
                        channelCache.put(oID, n);
                        newMap.remove(oID);
                        continue;
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
            newMap.clear();

            logger.finer("Channellist updated");
            QueryEvent refresh = new QueryEvent.BasicDataEvent.RefreshChannels();
            refresh.setConnection(((QueryConnection) event.getConnection()));
            eventService.fireEvent(refresh);
        }
    }
}