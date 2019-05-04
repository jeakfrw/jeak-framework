package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendTextMessageChannel extends TS3NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(SendTextMessageChannel.class);

    @Inject
    public IServer server;

    @Inject
    public IDataCache dataCache;

    @Override
    public int lowestUrgency() {
        return Urgency.BASIC.getLevel();
    }

    @Override
    public int highestUrgency() {
        return Urgency.ALERT.getLevel();
    }

    @Override
    public int lowestLifespan() {
        return Lifespan.SHORTER.getLevel();
    }

    @Override
    public int highestLifespan() {
        return Lifespan.LONGER.getLevel();
    }

    @Override
    public void sendNotification(INotification notification) {
        final QueryBuilder builder = IQueryRequest.builder()
                .command("sendtextmessage")
                .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.CLIENT.getQueryNum())
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
