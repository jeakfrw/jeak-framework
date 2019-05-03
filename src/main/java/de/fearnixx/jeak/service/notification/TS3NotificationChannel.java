package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Optional;

public abstract class TS3NotificationChannel implements INotificationChannel {

    protected abstract IDataCache getDataCache();

    protected void addRecipients(INotification notification, QueryBuilder builder) {
        final Iterator<String> it = notification.getRecipients().iterator();
        while (true) {
            final String uniqueId = it.next();

            final Optional<IClient> client = getDataCache().getClients()
                    .stream()
                    .filter(c -> c.getClientUniqueID().equals(uniqueId))
                    .findFirst();

            if (client.isPresent()) {
                builder.addKey(PropertyKeys.TextMessage.TARGET_ID, client.get().getClientID());

                if (it.hasNext()) {
                    builder.commitChainElement();
                } else {
                    break;
                }
            }
        }
    }

    protected void addLogging(QueryBuilder builder, Logger logger) {
        builder.onSuccess(a -> logger.debug("Successfully dispatched notification."));
        builder.onError(a -> {
            final IRawQueryEvent.IMessage.IErrorMessage error = a.getError();
            logger.warn("Failed to dispatch notification: {} {}", error.getCode(), error.getMessage());
        });
    }
}
