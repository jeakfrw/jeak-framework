package de.fearnixx.t3.teamspeak.cache;

import de.fearnixx.t3.service.task.ITask;
import de.fearnixx.t3.task.TaskService;
import de.fearnixx.t3.teamspeak.data.IChannel;
import de.fearnixx.t3.teamspeak.data.IClient;
import de.fearnixx.t3.teamspeak.data.TS3Channel;
import de.fearnixx.t3.teamspeak.data.TS3Client;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.QueryConnection;
import de.mlessmann.logging.ILogReceiver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class DataCache {

    private final Object lock = new Object();
    private ILogReceiver logger;

    private IQueryConnection connection;

    private Map<Integer, TS3Client> clientCache;
    private Map<Integer, TS3Channel> channelCache;

    public DataCache(ILogReceiver logger, IQueryConnection connection) {
        this.logger = logger;
        this.connection = connection;
        clientCache = new HashMap<>(50);
        channelCache = new HashMap<>(60);
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
                                              .runnable(() -> {
                                                  connection.sendRequest(clientListRequest);
                                              })
                                              .build();

    // == CHANNELLIST == //
    private final IQueryRequest channelListRequest = IQueryRequest.builder()
                                                                  .command("channellist")
                                                                  .addOption("-topic")
                                                                  .addOption("-flags")
                                                                  .addOption("-voice")
                                                                  .addOption("-limits")
                                                                  .addOption("-icons")
                                                                  .build();
    private final ITask channelListTask = ITask.builder()
                                               .name("t3server.channelrefresh")
                                               .interval(3L, TimeUnit.MINUTES)
                                               .runnable(() -> {
                                                   connection.sendRequest(channelListRequest);
                                               })
                                               .build();

    public void scheduleTasks(TaskService tm) {
        tm.runTask(clientListTask);
        tm.runTask(channelListTask);
    }


    private void reset() {
        synchronized (lock) {
            clientCache.forEach((clid, c) -> c.invalidate());
            clientCache.clear();
            channelCache.forEach((cid, c) -> c.invalidate());
            channelCache.clear();
        }
    }

    public Map<Integer, IClient> getClients() {
        synchronized (lock) {
            return new HashMap<>(clientCache);
        }
    }

    public Map<Integer, IChannel> getChannels() {
        synchronized (lock) {
            return new HashMap<>(channelCache);
        }
    }

    /*
    @Listener
    public void onNotify(IQueryEvent.INotification event) {

        if (event instanceof IQueryEvent.INotification.IClientMoved) {
            // Client has moved - Apply to representation
            synchronized (lock) {
                for (IQueryMessageObject msgObj : event.getObjects()) {
                    Integer clientID = Integer.parseInt(msgObj.getProperty("clid").orElse("-1"));
                    TS3Client client = clientCache.getOrDefault(clientID, null);
                    if (client == null)
                        continue;

                    TS3Channel fromChannel = channelCache.getOrDefault(client.getChannelID(), null);
                    TS3Channel toChannel = channelCache.getOrDefault(
                    Integer.parseInt(msgObj.getProperty("ctid").get())
                    , null);
                    if (fromChannel == null || toChannel == null || fromChannel == toChannel)
                        continue;


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
            }
        } else if (event instanceof IQueryEvent.INotification.IClientLeftView) {
            // Client has left - Apply to representation
            synchronized (lock) {
                Integer clientID = Integer.parseInt(event.getObjects().get(0).getProperty("clid").get());
                TS3Client client = clientCache.getOrDefault(clientID, null);
                if (client == null)
                    return;
                client.invalidate();
                clientCache.remove(clientID);
            }
        }
    }


    @Listener
    public void onQueryMessage(RawQueryEvent.Message.Answer event) {
        if (event.getError().getCode() != 0)
            return;
        if (event.getRequest() == clientListRequest) {
            List<IQueryMessageObject> objects = event.getObjects();
            // Just a lock to be used when the new or old mapping is accessed
            final Object tempLock = new Object();
            final Map<Integer, TS3Client> newMap = new HashMap<>(objects.size(), 1.1f);
            synchronized (lock) {
                objects.parallelStream().forEach(o -> {
                    try {
                        Integer cid = Integer.parseInt(o.getProperty(PropertyKeys.Client.ID).orElse("-1"));
                        if (cid == -1) {
                            log.warning("Skipping a client due to invalid ID");
                            return;
                        }
                        TS3Client c;
                        synchronized (tempLock) {
                            c = clientCache.getOrDefault(cid, null);
                        }
                        if (c == null) {
                            // Client is new - New reference
                            c = new TS3Client();
                            c.copyFrom(o);
                        } else {
                            // Client not new - Update values
                            c.copyFrom(o);
                        }
                        synchronized (tempLock) {
                            newMap.put(cid, c);
                        }
                    } catch (Exception e) {
                        log.warning("Failed to parse a client", e);
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

            log.finer("Clientlist updated");
            eventService.fireEvent(new TS3ServerEvent.DataEvent.ClientsUpdated(TS3Server.this));

        } else if (event.getRequest() == channelListRequest) {
            List<IQueryMessageObject> objects = event.getObjects();
            // Just a lock to be used when the new or old mapping is accessed
            final Object tempLock = new Object();
            final Map<Integer, TS3Channel> newMap = new HashMap<>(objects.size(), 1.1f);
            synchronized (lock) {
                objects.parallelStream().forEach(o -> {
                    try {
                        Integer cid = Integer.parseInt(o.getProperty(PropertyKeys.Channel.ID).orElse("-1"));
                        if (cid == -1) {
                            log.warning("Skipping a channel due to invalid channel ID");
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
                                log.warning("Skipping a channel due to missing name");
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
                        synchronized (tempLock) {
                            newMap.put(cid, c);
                        }
                    } catch (Exception e) {
                        log.warning("Failed to parse a channel", e);
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
                        log.warning("Channel", cid, "has nonexistent parent", c.getParent());
                        return;
                    }
                    parent.addSubChannel(c);
                });
            }
            newMap.clear();

            log.finer("Channellist updated");
            eventService.fireEvent(new TS3ServerEvent.DataEvent.ChannelsUpdated(TS3Server.this));
        }
    }
    */
}