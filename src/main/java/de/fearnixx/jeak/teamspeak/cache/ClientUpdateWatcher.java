package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.TS3Channel;
import de.fearnixx.jeak.teamspeak.data.TS3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ClientUpdateWatcher {

    private static final Logger logger = LoggerFactory.getLogger(ClientUpdateWatcher.class);

    private final Object lock;
    private final DataCache dataCache;

    public ClientUpdateWatcher(Object lock, DataCache dataCache) {
        this.lock = lock;
        this.dataCache = dataCache;
    }

    /**
     * Generic listeners to update caches.
     * This HAVE TO run after normal event processing!
     */
    @Listener(order = Listener.Orders.LATEST)
    public void onNotify(IQueryEvent.INotification event) {
        if (event instanceof IQueryEvent.INotification.IClientMoved) {
            // Client has moved - Apply to representation
            final Map<Integer, TS3Client> internalCache = dataCache.unsafeGetClients();
            synchronized (lock) {
                Integer clientID = Integer.valueOf(event.getProperty("clid").orElse("-1"));
                TS3Client client = internalCache.getOrDefault(clientID, null);
                if (client == null) {
                    logger.warn("Insufficient information for clientMoved update: Client not yet cached.");
                    return;
                }

                Map<Integer, TS3Channel> channelCache = dataCache.unsafeGetChannels();
                TS3Channel fromChannel = channelCache.getOrDefault(client.getChannelID(), null);
                Integer fromChannelId = fromChannel != null ? fromChannel.getID() : -1;
                TS3Channel toChannel = channelCache.getOrDefault(
                        Integer.parseInt(event.getProperty("ctid")
                                .orElseThrow(() -> new IllegalStateException("Missing ctid in ClientMoved!"))),
                        null);
                Integer toChannelId = toChannel != null ? toChannel.getID() : -1;
                if (fromChannel == null || toChannel == null) {
                    logger.warn("Insufficient information for clientMoved update: {} -> {}", fromChannelId, toChannelId);
                    return;
                }

                logger.debug("Updating cached client {} \"{}\" | Channel: {} -> {}",
                        clientID, PropertyKeys.Client.CHANNEL_ID, fromChannelId, toChannelId);

                // Set new channel
                client.setProperty(PropertyKeys.Client.CHANNEL_ID, toChannelId.toString());
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
            synchronized (lock) {
                Map<Integer, TS3Client> clientCache = dataCache.unsafeGetClients();
                Integer clientID = Integer.parseInt(event.getProperty("clid")
                        .orElseThrow(() -> new IllegalStateException("Missing clid in ClientLeave!")));
                TS3Client client = clientCache.getOrDefault(clientID, null);
                if (client == null) {
                    return;
                }
                client.invalidate();
                clientCache.remove(clientID);
                logger.debug("Invalidated client: {}", client);
            }
        }
    }
}
