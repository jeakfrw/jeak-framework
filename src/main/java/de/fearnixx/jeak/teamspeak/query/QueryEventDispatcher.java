package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.EventCaptions;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import de.fearnixx.jeak.teamspeak.except.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Dispatches events based on incoming {@link de.fearnixx.jeak.event.IRawQueryEvent}s.
 */
public class QueryEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(QueryEventDispatcher.class);

    private static final List<String> STD_CHANNELEDIT_PROPS = Arrays.asList(
            PropertyKeys.Channel.ID,
            PropertyKeys.TextMessage.SOURCE_ID,
            PropertyKeys.TextMessage.SOURCE_NICKNAME,
            PropertyKeys.TextMessage.SOURCE_UID,
            "reasonid"
    );

    @Inject
    public IEventService eventService;

    private int lastNotificationHash;
    private IRawQueryEvent.IMessage.INotification pendingEditEvent;

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
                notification = checkEditSuspend(event);
                break;

            case EventCaptions.CHANNEL_EDITED_DESCR:
                if (editResume(event)) {
                    return;
                } else {
                    notification = new QueryEvent.ChannelEditDescr();
                    break;
                }

            case EventCaptions.CHANNEL_EDITED_PASSWORD:
                if (editResume(event)) {
                    return;
                } else {
                    notification = new QueryEvent.ChannelPasswordChanged();
                    break;
                }

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
                    case 1:
                        notification = new QueryEvent.ClientTextMessage();
                        break;
                    case 2:
                        notification = new QueryEvent.ChannelTextMessage();
                        break;
                    case 3:
                        notification = new QueryEvent.ServerTextMessage();
                        break;
                    default:
                        throw new QueryException("Unknown message targetMode: " + mode);
                }
                break;

            default:
                throw new QueryException("Unknown event: " + caption);
        }

        // === Possible valid skips === //
        if (notification == null) {
            logger.debug("No event type determined. Skipping dispatching.");
            return;
        } else if (checkHash && hashCode == lastNotificationHash) {
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

    private QueryEvent.ChannelEdit checkEditSuspend(IRawQueryEvent.IMessage.INotification event) {
        final Map<String, String> deltas = new HashMap<>();
        boolean deltaFound = false;
        for (String key : event.getValues().keySet()) {
            if (!STD_CHANNELEDIT_PROPS.contains(key)) {
                logger.debug("ChannelEdit delta found: {}", key);
                deltaFound = true;
                deltas.put(key, event.getValues().get(key));
            }
        }

        if (!deltaFound) {
            logger.debug("Intermitting channelEdit event. No delta found.");
            pendingEditEvent = event;
            return null;
        } else {
            return new QueryEvent.ChannelEdit(deltas);
        }
    }

    private boolean editResume(IRawQueryEvent.IMessage.INotification event) {
        if (pendingEditEvent == null) {
            logger.error("Failed to resume channelEdit event! No pending event found!");
            return true;
        } else {
            // Merge with last event
            // This allows us to capture multi-edits across normal properties and descr/password
            event.merge(pendingEditEvent);
            return false;
        }
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
