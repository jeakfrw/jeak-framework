package de.fearnixx.t3.event;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public interface IEventManager {

    void fireEvent(IEvent event);
    void registerListener(Object listener);
    void registerListeners(Object... listeners);
}
