package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.query.QueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.except.QueryException;
import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class QueryNotifier {

    @Inject
    public ILogReceiver logger;

    @Inject
    public IEventService eventService;

    private Integer lastHash;

    public void processEvent(RawQueryEvent event, Integer hashCode) throws QueryException {

        // Fire the RAW event. (Will allow manipulation)
        // This WILL also fire for unknown events ;)
        eventService.fireEvent(event);

        if (event instanceof RawQueryEvent.Message.Notification) {
            RawQueryEvent.Message.Notification rawNotification = ((RawQueryEvent.Message.Notification) event);
            QueryEvent.Notification notification;
            String caption = rawNotification.getCaption().toLowerCase();

            boolean checkHash = true;

            switch (caption) {
                case "cliententerview":
                    notification = new QueryEvent.ClientEnter();
                    break;
                case "clientleftview":
                    notification = new QueryEvent.ClientLeave();
                    break;
                case "clientmoved":
                    notification = new QueryEvent.ClientMoved();
                    break;
                case "channelcreated":
                    notification = new QueryEvent.ChannelCreate();
                    break;
                case "channeledited":
                    notification = new QueryEvent.ChannelEdit();
                    break;
                case "channeldeleted":
                    notification = new QueryEvent.ChannelDelete();
                    break;
                case "textmessage":
                    checkHash = false;
                    Integer mode = Integer.parseInt(event.getProperty(PropertyKeys.TextMessage.TARGET_TYPE).get());
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

            if (checkHash && hashCode.equals(lastHash)) {
                logger.finer("Dropping duplicate ", caption);
                return;
            }
            lastHash = hashCode;

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
