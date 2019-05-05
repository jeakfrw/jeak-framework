package de.fearnixx.jeak.controller;

import de.fearnixx.jeak.controller.events.ControllerEvent;
import de.fearnixx.jeak.controller.interfaces.IRestControllerManager;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.event.IEventService;

import java.util.Map;
import java.util.Optional;

public class RestControllerManager implements IRestControllerManager {
    private final Map<Class<?>, Object> controllers;

    public RestControllerManager(Map<Class<?>, Object> controllers) {
        this.controllers = controllers;
    }

    @Inject
    private IEventService eventService;

    @Override
    public <T> void registerController(Class<T> cntrlrClass, T restController) {
        controllers.put(cntrlrClass, restController);
        eventService.fireEvent(new ControllerEvent.ControllerRegisteredEvent<>(restController));
    }

    @Override
    public <T> Optional<T> provide(Class<T> cntrlrClass) {
        Object cntrlr = controllers.getOrDefault(cntrlrClass, null);
        return Optional.ofNullable((T) cntrlr);
    }

    @Override
    public <T> T provideUnchecked(Class<T> cntrlrClass) {
        return (T) controllers.get(cntrlrClass);
    }

    @Override
    public Map<Class<?>, Object> provideAll() {
        return controllers;
    }

}
