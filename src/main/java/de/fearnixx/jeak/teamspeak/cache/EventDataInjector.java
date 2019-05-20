package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.event.EventAbortException;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.TS3Channel;
import de.fearnixx.jeak.teamspeak.data.TS3Client;
import de.fearnixx.jeak.teamspeak.data.TS3Spacer;

import java.util.Optional;

public class EventDataInjector {

    private final Object LOCK;
    private final DataCache dataCache;

    public EventDataInjector(Object lock, DataCache dataCache) {
        this.LOCK = lock;
        this.dataCache = dataCache;
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
            QueryEvent.ClientEnter enterEvent = (QueryEvent.ClientEnter) event;
            TS3Client client = new TS3Client();
            client.copyFrom(event);

            // Client-Enter events use "ctid" and not "cid".
            client.setProperty("cid", enterEvent.getTargetChannelId());
            // Reset `cliententerview` specific properties.
            client.setProperty("ctid", null);
            client.setProperty("cfid", null);
            synchronized (LOCK) {
                dataCache.unsafeGetClients().put(client.getClientID(), client);
            }
        }

        String targetPropertyName = PropertyKeys.Client.ID;
        if (event instanceof IQueryEvent.INotification.ITextMessage) {
            targetPropertyName = PropertyKeys.TextMessage.SOURCE_ID;
        }

        Optional<String> optClientID = event.getProperty(targetPropertyName);
        if (optClientID.isPresent()) {
            Integer clientID = Integer.valueOf(optClientID.get());

            TS3Client client;
            synchronized (LOCK) {
                client = dataCache.unsafeGetClients().getOrDefault(clientID, null);
            }

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
            synchronized (LOCK) {
                dataCache.unsafeGetChannels().put(channel.getID(), channel);
            }
        }

        Optional<String> optChannelID = event.getProperty("cid");
        if (optChannelID.isPresent()) {
            Integer channelID = Integer.valueOf(optChannelID.get());
            TS3Channel client;
            synchronized (LOCK) {
                client = dataCache.unsafeGetChannels().getOrDefault(channelID, null);
            }

            if (client != null) {
                event.setChannel(client);
                return;
            }
        }
        throw new EventAbortException("Target/Channel injection failed! Event aborted!");
    }
}
