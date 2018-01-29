package de.fearnixx.t3.service.event;

import de.fearnixx.t3.event.IEvent;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public interface IEventService {

    void fireEvent(IEvent event);
    void registerListener(Object listener);
    void registerListeners(Object... listeners);
}
