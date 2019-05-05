package de.fearnixx.jeak.controller.events;

public class ControllerEvent implements IControllerEvent {
    public static class ControllerRegisteredEvent<T> extends ControllerEvent implements IControllerEvent.IControllerRegistered<T> {
        private T controller;

        public ControllerRegisteredEvent(T controller) {
            this.controller = controller;
        }

        @Override
        public T getController() {
            return controller;
        }

    }
}
