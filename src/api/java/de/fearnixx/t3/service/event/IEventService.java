package de.fearnixx.t3.service.event;

import de.fearnixx.t3.event.IEvent;

/**
 * Accessor for plugins to fire custom events so other plugins/classes may listen to it.
 */
public interface IEventService {

    void fireEvent(IEvent event);
    void registerListener(Object listener);
    void registerListeners(Object... listeners);
}
