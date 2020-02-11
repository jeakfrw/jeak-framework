package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.except.EventAbortException;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.teamspeak.TS3UserSubject;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.TS3Channel;
import de.fearnixx.jeak.teamspeak.data.TS3Client;
import de.fearnixx.jeak.teamspeak.data.TS3Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class EventDataInjector {

    private static final Logger logger = LoggerFactory.getLogger(EventDataInjector.class);

    private final Object LOCK;
    private final DataCache dataCache;

    @Inject
    private IPermissionService permService;

    @Inject
    private IProfileService profileSvc;

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
            applyPermissions(client);

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

    private void applyPermissions(TS3Client client) {
        String ts3uid = client.getClientUniqueID();
        UUID uuid = profileSvc.getOrCreateProfile(ts3uid)
                .map(IUserProfile::getUniqueId)
                .orElseThrow(() -> new IllegalStateException("Failed to reserve profile UUID for subject: " + client));
        logger.debug("Client {} got permission UUID: {}", client, uuid);
        final TS3UserSubject ts3Subject = new TS3UserSubject(permService.getTS3Provider(), client.getClientDBID());
        client.setTs3PermSubject(ts3Subject);
        client.setFrameworkSubjectUUID(uuid);
        client.setFrwPermProvider(permService.getFrameworkProvider());
    }

    private void processTargetChannel(QueryEvent.Notification.TargetChannel event) {
        if (event instanceof QueryEvent.Notification.TargetChannel.ChannelCreate) {
            String channelName = event.getProperty("channel_name")
                    .orElseThrow(() -> new IllegalStateException("Missing channel_name in ChannelCreate event."));
            boolean isSpacer = TS3Spacer.spacerPattern.matcher(channelName).matches();
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
