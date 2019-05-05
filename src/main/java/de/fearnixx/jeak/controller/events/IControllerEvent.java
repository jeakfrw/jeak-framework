package de.fearnixx.jeak.controller.events;

import de.fearnixx.jeak.event.IEvent;

public interface IControllerEvent extends IEvent {
    public interface IControllerRegistered<T> extends IControllerEvent {
        T getController();
    }
}
