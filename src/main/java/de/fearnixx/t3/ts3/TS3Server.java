package de.fearnixx.t3.ts3;

import de.fearnixx.t3.event.IEventManager;
import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.event.server.TS3ServerEvent;
import de.fearnixx.t3.ts3.keys.NotificationType;
import de.fearnixx.t3.ts3.query.IQueryConnection;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.query.IQueryRequest;
import de.fearnixx.t3.reflect.annotation.Listener;
import de.fearnixx.t3.task.ITask;
import de.fearnixx.t3.task.TaskManager;
import de.fearnixx.t3.ts3.channel.IChannel;
import de.fearnixx.t3.ts3.channel.TS3Channel;
import de.fearnixx.t3.ts3.channel.TS3Spacer;
import de.fearnixx.t3.ts3.client.IClient;
import de.fearnixx.t3.ts3.client.TS3Client;
import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.query.QueryConnectException;
import de.fearnixx.t3.ts3.query.QueryConnection;
import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class TS3Server implements ITS3Server {

    private ILogReceiver log;
    private IEventManager eventMgr;

    private volatile String host;
    private int port;
    private String user;
    private String pass;
    private int instID;
    private final QueryConnection mainConnection;
    private final DataManager dm;

    public TS3Server(IEventManager eventMgr, ILogReceiver log) {
        this.log = log;
        this.eventMgr = eventMgr;
        dm = this.new DataManager();
        mainConnection = new QueryConnection(eventMgr, log.getChild("NET"), this::onClose);
    }

    public void connect(String host, int port, String user, String pass, int instID) throws QueryConnectException {
        if (this.host!=null) {
            throw new RuntimeException("Can only connect a server once!");
        }
        this.instID = instID;
        this.user = user;
        this.pass = pass;
        this.port = port;
        this.host = host;
        mainConnection.setHost(host, port);
        try {
            mainConnection.open();
        } catch (IOException e) {
            throw new QueryConnectException("Unable to open QueryConnection", e);
        }
        mainConnection.start();
        if (!mainConnection.blockingLogin(instID, user, pass)) {
            throw new QueryConnectException("BlockingLogin failed: See log");
        }
        mainConnection.subscribeNotification(NotificationType.CLIENT_ENTER);
        mainConnection.subscribeNotification(NotificationType.CLIENT_LEAVE);
        mainConnection.subscribeNotification(NotificationType.TEXT_PRIVATE);
        mainConnection.subscribeNotification(NotificationType.TEXT_SERVER, mainConnection.getInstanceID());

        eventMgr.registerListener(dm);
    }

    public void scheduleTasks(TaskManager tm) {
        tm.runTask(dm.clientListTask);
        tm.runTask(dm.channelListTask);
    }

    /* * * RUNTIME CONTROL * * */

    private void onClose(IQueryConnection conn) {
        if (conn == mainConnection) {
            shutdown();
        }
    }

    public void shutdown() {
        dm.reset();
        mainConnection.kill();
    }

    /* * * MISC * * */

    public IQueryConnection getConnection() {
        return mainConnection;
    }

    @Override
    public Map<Integer, IClient> getClientMap() { return Collections.unmodifiableMap(dm.getClients()); }

    @Override
    public List<IClient> getClientList() {
        return Collections.unmodifiableList(new ArrayList<>(dm.getClients().values()));
    }

    @Override
    public Map<Integer, IChannel> getChannelMap() {
        return Collections.unmodifiableMap(dm.getChannels());
    }

    @Override
    public List<IChannel> getChannelList() {
        return Collections.unmodifiableList(new ArrayList<>(dm.getChannels().values()));
    }

    /* * * DATA MANAGER * * */

    public class DataManager {

        private final Object lock = new Object();

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
                    mainConnection.sendRequest(clientListRequest);
                })
                .build();
        private Map<Integer, TS3Client> clientMap;

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
                    mainConnection.sendRequest(channelListRequest);
                })
                .build();
        private Map<Integer, TS3Channel> channelMap;

        private DataManager() {
            clientMap = new HashMap<>(50);
            channelMap = new HashMap<>(60);
        }

        private void reset() {
            synchronized (lock) {
                clientMap.forEach((clid, c) -> c.invalidate());
                clientMap.clear();
                channelMap.forEach((cid, c) -> c.invalidate());
                channelMap.clear();
            }
        }

        public Map<Integer, IClient> getClients() {
            synchronized (lock) {
                return new HashMap<>(clientMap);
            }
        }

        public Map<Integer, IChannel> getChannels() {
            synchronized (lock) {
                return new HashMap<>(channelMap);
            }
        }

        @Listener
        public void onQueryMessage(IQueryEvent.IMessage event) {
            if (event.getMessage().getError().getID() != 0)
                return;
            if (event.getRequest() == clientListRequest) {
                List<IQueryMessageObject> objects = event.getMessage().getObjects();
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
                                c = clientMap.getOrDefault(cid, null);
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
                    Integer[] cIDs = clientMap.keySet().toArray(new Integer[clientMap.keySet().size()]);
                    for (int i = clientMap.size() - 1; i >= 0; i--) {
                        oID = cIDs[i];
                        o = clientMap.get(oID);
                        n = newMap.getOrDefault(oID, null);
                        if (n == null) {
                            // Client removed - invalidate & remove
                            o.invalidate();
                            channelMap.remove(oID);
                            continue;
                        } else {
                            newMap.remove(oID);
                            continue;
                        }
                    }
                    // All others are new - Add them
                    newMap.forEach(clientMap::put);
                    newMap.clear();
                }

                log.finer("Clientlist updated");
                eventMgr.fireEvent(new TS3ServerEvent.DataEvent.ClientsUpdated(TS3Server.this));

            } else if (event.getRequest() == channelListRequest) {
                List<IQueryMessageObject> objects = event.getMessage().getObjects();
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
                                c = channelMap.getOrDefault(cid, null);
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
                    Integer[] cIDs = channelMap.keySet().toArray(new Integer[channelMap.keySet().size()]);
                    for (int i = channelMap.size() - 1; i >= 0; i--) {
                        oID = cIDs[i];
                        o = channelMap.get(oID);
                        o.clearChildren();
                        n = newMap.getOrDefault(oID, null);
                        if (n == null) {
                            // Channel removed - invalidate & remove
                            o.invalidate();
                            channelMap.remove(oID);
                            continue;
                        } else if (n == o) {
                            // Channel unchanged - continue
                            newMap.remove(oID);
                            continue;
                        } else {
                            // Channel reference updated - invalidate & change
                            o.invalidate();
                            channelMap.put(oID, n);
                            newMap.remove(oID);
                            continue;
                        }
                    }
                    // All others are new - Add them
                    newMap.forEach(channelMap::put);
                    channelMap.forEach((cid, c) -> {
                        int pid = c.getParent();
                        if (pid == 0) return;
                        TS3Channel parent = channelMap.getOrDefault(pid, null);
                        if (parent == null) {
                            log.warning("Channel", cid, "has nonexistent parent", c.getParent());
                            return;
                        }
                        parent.addSubChannel(c);
                    });
                }
                newMap.clear();

                log.finer("Channellist updated");
                eventMgr.fireEvent(new TS3ServerEvent.DataEvent.ChannelsUpdated(TS3Server.this));
            }
        }
    }
}
