package de.fearnixx.jeak.service.event;

import de.fearnixx.jeak.event.IEvent;

/**
 * Accessor for plugins to fire custom events so other plugins/classes may listen to it.
 */
public interface IEventService {

    /**
     * Allows you to fire an event.
     * Implementing {@link IEvent} for custom events is supported and endorsed.
     */
    void fireEvent(IEvent event);

    /**
     * Register a listener.
     * All methods annotated with {@link de.fearnixx.jeak.reflect.Listener} will receive events fitting their parameter.
     */
    void registerListener(Object listener);

    /**
     * @see #registerListener(Object)
     */
    void registerListeners(Object... listeners);

    /**
     * Opposite of {@link #registerListener(Object)}.
     * Be aware that we are comparing by identity!
     */
    void unregisterListener(Object listener);
}
