package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.TS3Client;
import de.fearnixx.jeak.teamspeak.data.TS3ClientHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.util.TS3DataFixes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClientCache {

    private static final Logger logger = LoggerFactory.getLogger(ClientCache.class);

    private final Map<Integer, TS3Client> clientCache = new ConcurrentHashMap<>(50);
    private final Object LOCK;

    @Inject
    private IServer server;

    @Inject
    private IEventService eventService;

    @Inject
    private ITaskService taskService;

    // == CLIENTLIST = //

    private final IQueryRequest clientListRequest = IQueryRequest.builder()
            .command(QueryCommands.CLIENT.CLIENT_LIST)
            .addOption("-uid")
            .addOption("-away")
            .addOption("-voice")
            .addOption("-times")
            .addOption("-groups")
            .addOption("-info")
            .addOption("-icon")
            .addOption("-country")
            .onDone(this::onListAnswer)
            .build();
    private final ITask clientListTask = ITask.builder()
            .name("cache.clientRefresh")
            .interval(DataCache.CLIENT_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .runnable(() -> server.optConnection().ifPresent(conn -> conn.sendRequest(clientListRequest)))
            .build();

    public ClientCache(Object lock) {
        this.LOCK = lock;
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        taskService.runTask(clientListTask);
    }

    @Listener(order = Listener.Orders.LATEST)
    public void onDisconnected(IBotStateEvent.IConnectStateEvent.IDisconnect event) {
        synchronized (LOCK) {
            logger.info("Clearing client cache due to disconnect.");
            taskService.removeTask(clientListTask);
            clientCache.values().forEach(TS3ClientHolder::invalidate);
            clientCache.clear();
        }
    }

    private void onListAnswer(IQueryEvent.IAnswer event) {
        if (event.getRequest() == clientListRequest) {
            if (event.getErrorCode() == 0) {
                refreshClients((IRawQueryEvent.IMessage.IAnswer) event.getRawReference());
            } else {
                logger.warn("Client-List data refresh returned an error: {} - {}", event.getErrorCode(), event.getErrorMessage());
            }
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
                    clientCache.remove(oID);

                } else {
                    clientMapping.remove(oID);
                }
            }

            // All others are new - Add them
            clientMapping.forEach(clientCache::put);
            clientMapping.clear();
        }

        logger.debug("Clientlist updated");
        QueryEvent refresh = new QueryEvent.BasicDataEvent
                .RefreshClients(getClients(), getClientMap());
        refresh.setConnection(event.getConnection());
        refresh.setRawReference(event);
        eventService.fireEvent(refresh);
    }

    /**
     * Creates a Map of all available clients from a `clientlist` answer event.
     * Helper method for {@link #refreshClients(IRawQueryEvent.IMessage.IAnswer)}.
     * <p>
     * Handles update existing and creating clients
     */
    private Map<Integer, TS3Client> generateClientMapping(List<IRawQueryEvent.IMessage> messageObjects) {
        final Map<Integer, TS3Client> mapping = new HashMap<>(messageObjects.size(), 1.1f);
        messageObjects
                .stream()
                .parallel()
                .forEach(message -> {
                    try {
                        int cid = Integer.parseInt(message.getProperty(PropertyKeys.Client.ID).orElse("-1"));

                        if (cid == -1) {
                            logger.warn("Skipping a client due to invalid ID: {}",
                                    message.getProperty(PropertyKeys.Client.ID)
                                            .orElse("null"));
                            return;
                        }

                        TS3Client client = clientCache.getOrDefault(cid, null);

                        if (client == null) {
                            // Client is new - New reference
                            client = new TS3Client();
                            client.copyFrom(message);
                            logger.debug("Created new client representation for {}/{}",
                                    client.getClientUniqueID(), client.getNickName());

                        } else {
                            // Client not new - Update values
                            client.merge(message);
                        }

                        // Fix client icon ID in case it got misread by TS3
                        TS3DataFixes.ICONS_INVALID_CRC32(client, PropertyKeys.Client.ICON_ID);
                        mapping.put(cid, client);
                    } catch (Exception e) {
                        logger.warn("Failed to parse a client", e);
                    }
                });
        return mapping;
    }

    public Map<Integer, IClient> getClientMap() {
        synchronized (LOCK) {
            return Collections.unmodifiableMap(clientCache);
        }
    }

    public List<IClient> getClients() {
        synchronized (LOCK) {
            return Collections.unmodifiableList(new ArrayList<>(clientCache.values()));
        }
    }

    Map<Integer, TS3Client> getUnsafeClientMap() {
        synchronized (LOCK) {
            return clientCache;
        }
    }
}
