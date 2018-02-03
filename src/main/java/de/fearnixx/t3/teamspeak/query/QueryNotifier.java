package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.query.QueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.event.EventService;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.except.QueryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class QueryNotifier {

    private EventService eventService;

    public QueryNotifier(EventService eventService) {
        this.eventService = eventService;
    }

    public void processEvent(RawQueryEvent event) throws QueryException {

        // Fire the RAW event. (Will allow manipulation)
        // This WILL also fire for unknown events ;)
        eventService.fireEvent(event);

        if (event instanceof RawQueryEvent.Message.Notification) {
            RawQueryEvent.Message.Notification rawNotification = ((RawQueryEvent.Message.Notification) event);
            QueryEvent.Notification notification;
            String caption = rawNotification.getCaption();

            switch (caption) {
                case "cliententer":
                    notification = new QueryEvent.ClientEnter();
                    break;
                case "clientleave":
                    notification = new QueryEvent.ClientLeave();
                    break;
                case "clientmove":
                    notification = new QueryEvent.ClientMove();
                    break;
                case "channelcreate":
                    notification = new QueryEvent.ChannelCreate();
                    break;
                case "channeledit":
                    notification = new QueryEvent.ChannelEdit();
                    break;
                case "channeldelete":
                    notification = new QueryEvent.ChannelDelete();
                    break;
                case "textmessage":
                    Integer mode = Integer.parseInt(event.getProperty(PropertyKeys.TextMessage.TARGET_TYPE).get());
                    switch (mode) {
                        case 1: notification = new QueryEvent.ClientTextMessage();
                        case 2: notification = new QueryEvent.ChannelTextMessage();
                        case 3: notification = new QueryEvent.ServerTextMessage();
                        default: throw new QueryException("Unknown message targetMode: " + mode);
                    }
                default:
                    throw new QueryException("Unknown event: " + caption);
            }

            notification.setConnection(event.getConnection());
            notification.setCaption(caption);

            // Loop through chained notifications.
            // Handling inside plugins will be simpler if we do the loop.
            // (Note for future: This may not work asynchronously)
            RawQueryEvent.Message msg = ((RawQueryEvent.Message) event);
            do {
                notification.copyFrom(msg);
                eventService.fireEvent(notification);
            } while ((msg = msg.getNext()) != null);

        } else if (event instanceof RawQueryEvent.Message.Answer) {
            RawQueryEvent.Message.Answer rawAnswer = ((RawQueryEvent.Message.Answer) event);
            QueryEvent.Answer answer = new QueryEvent.Answer();
            answer.setConnection(rawAnswer.getConnection());
            answer.setRequest(rawAnswer.getRequest());

            List<IDataHolder> dataHolders = new ArrayList<>(rawAnswer.toList());
            answer.setChain(Collections.unmodifiableList(dataHolders));

            // Fire the processed event
            eventService.fireEvent(answer);
        } else {
            throw new QueryException("Unknown query event class: " + event.getClass().getName());
        }
    }
}
