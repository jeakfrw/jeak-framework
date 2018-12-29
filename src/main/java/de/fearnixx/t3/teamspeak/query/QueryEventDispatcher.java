package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.QueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.teamspeak.EventCaptions;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.except.ConsistencyViolationException;
import de.fearnixx.t3.teamspeak.except.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dispatches events based on incoming {@link de.fearnixx.t3.event.IRawQueryEvent}s.
 */
public class QueryEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(QueryEventDispatcher.class);

    @Inject
    public IEventService eventService;

    private int lastNotificationHash;

    public void dispatchNotification(IRawQueryEvent.IMessage.INotification event) {
        QueryEvent.Notification notification;
        String caption = event.getCaption().toLowerCase();
        int hashCode = event.getHashCode();

        boolean checkHash = true;

        switch (caption) {
            case EventCaptions.CLIENT_ENTER:
                notification = new QueryEvent.ClientEnter();
                break;

            case EventCaptions.CLIENT_LEFT:
                notification = new QueryEvent.ClientLeave();
                break;

            case EventCaptions.CLIENT_MOVED:
                notification = new QueryEvent.ClientMoved();
                break;

            case EventCaptions.CHANNEL_CREATED:
                notification = new QueryEvent.ChannelCreate();
                break;

            case EventCaptions.CHANNEL_EDITED:
                notification = new QueryEvent.ChannelEdit();
                break;

            case EventCaptions.CHANNEL_EDITED_DESCR:
                notification = new QueryEvent.ChannelEditDescr();
                break;

            case EventCaptions.CHANNEL_DELETED:
                notification = new QueryEvent.ChannelDelete();
                break;

            case EventCaptions.TEXT_MESSAGE:
                checkHash = false;
                String strMode =
                        event.getProperty(PropertyKeys.TextMessage.TARGET_TYPE)
                                .orElseThrow(() -> new ConsistencyViolationException("TextMessage event without mode ID received!"));

                int mode = Integer.parseInt(strMode);
                switch (mode) {
                    case 1: notification = new QueryEvent.ClientTextMessage(); break;
                    case 2: notification = new QueryEvent.ChannelTextMessage(); break;
                    case 3: notification = new QueryEvent.ServerTextMessage(); break;
                    default: throw new QueryException("Unknown message targetMode: " + mode);
                }
                break;

            default:
                throw new QueryException("Unknown event: " + caption);
        }

        if (checkHash && hashCode == lastNotificationHash) {
            logger.debug("Dropping duplicate {}", caption);
            return;
        }
        lastNotificationHash = hashCode;

        notification.setConnection(event.getConnection());
        notification.setCaption(caption);

        // Loop through chained notifications.
        // Handling inside plugins will be simpler if we do the loop.
        // (Note for future: This may not work asynchronously)
        RawQueryEvent.Message msg = ((RawQueryEvent.Message) event);
        do {
            notification.merge(msg);
            eventService.fireEvent(notification);
        } while ((msg = msg.getNext()) != null);
    }

    public void dispatchAnswer(IRawQueryEvent.IMessage.IAnswer event) {
        IQueryRequest request = event.getRequest();

        QueryEvent.Answer answer = new QueryEvent.Answer();
        answer.setConnection(event.getConnection());
        answer.setRequest(request);
        answer.setError(event.getError());
        answer.setRawReference(event);

        List<IDataHolder> dataHolders = new ArrayList<>(event.toList());
        answer.setChain(Collections.unmodifiableList(dataHolders));

        // Callbacks are prioritized to event listeners
        invokeCallbacks(event, request, answer);

        // Fire the processed event
        eventService.fireEvent(answer);
    }

    private void invokeCallbacks(IRawQueryEvent.IMessage.IAnswer event, IQueryRequest request, QueryEvent.Answer answer) {
        int errorCode = event.getError().getCode();
        if (errorCode == 0 && request.onSuccess() != null) {
            request.onSuccess().accept(answer);
        } else if (errorCode != 0 && request.onError() != null) {
            request.onError().accept(answer);
        }

        if (request.onDone() != null) {
            request.onDone().accept(answer);
        }
    }
}
