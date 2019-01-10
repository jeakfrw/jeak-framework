package de.fearnixx.jeak.event;

/**
 * @author MarkL4YG
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

    public EventInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
