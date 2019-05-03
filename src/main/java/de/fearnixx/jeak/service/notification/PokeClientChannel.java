package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PokeClientChannel extends TS3NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(PokeClientChannel.class);

    @Inject
    public IDataCache dataCache;

    @Inject
    public IServer server;

    @Override
    public int lowestUrgency() {
        return Urgency.WARN.getLevel();
    }

    @Override
    public int highestUrgency() {
        return Urgency.ALERT.getLevel();
    }

    @Override
    public int lowestLifespan() {
        return 0;
    }

    @Override
    public int highestLifespan() {
        return Lifespan.SHORTER.getLevel();
    }

    @Override
    public void sendNotification(INotification notification) {
        QueryBuilder builder = IQueryRequest.builder()
                .command("clientpoke")
                .addKey(PropertyKeys.TextMessage.MESSAGE, notification.getShortText());

        addRecipients(notification, builder);

        addLogging(builder, logger);
        server.getConnection().sendRequest(builder.build());
    }

    @Override
    protected IDataCache getDataCache() {
        return dataCache;
    }
}
