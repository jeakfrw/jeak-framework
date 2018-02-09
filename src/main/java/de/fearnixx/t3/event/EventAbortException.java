package de.fearnixx.t3.event;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public class EventAbortException extends RuntimeException {

    public EventAbortException(String message) {
        super(message);
    }

    public EventAbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
