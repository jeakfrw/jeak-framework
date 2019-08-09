package de.fearnixx.jeak.event.except;

/**
 */
public class EventInvocationException extends RuntimeException {

    public EventInvocationException() {
    }

    public EventInvocationException(String message) {
        super(message);
    }

    public EventInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventInvocationException(Throwable cause) {
        super(cause);
    }
}
