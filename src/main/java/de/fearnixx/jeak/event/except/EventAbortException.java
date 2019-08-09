package de.fearnixx.jeak.event.except;

/**
 * Special exception that causes events to be immediately aborted causing no following listeners to be fired.
 */
public class EventAbortException extends RuntimeException {

    public EventAbortException() {
    }

    public EventAbortException(String message) {
        super(message);
    }

    public EventAbortException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventAbortException(Throwable cause) {
        super(cause);
    }

    public EventAbortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
