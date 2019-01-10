package de.fearnixx.jeak.event;

/**
 * Created by MarkL4YG on 01-Feb-18
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
