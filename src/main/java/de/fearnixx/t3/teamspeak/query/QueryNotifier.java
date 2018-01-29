package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.QueryEvent;
import de.fearnixx.t3.event.event.EventService;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class QueryNotifier {

    private EventService eventService;

    public void processEvent(QueryEvent event) {

        if (event instanceof QueryEvent.Message.Notification) {
            QueryEvent.Message.Notification rawNotification;

        }
    }
}
