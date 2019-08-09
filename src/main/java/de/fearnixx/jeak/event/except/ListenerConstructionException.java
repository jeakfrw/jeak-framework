package de.fearnixx.jeak.event.except;

public class ListenerConstructionException extends RuntimeException {

    public ListenerConstructionException() {
        super();
    }

    public ListenerConstructionException(String message) {
        super(message);
    }

    public ListenerConstructionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ListenerConstructionException(Throwable cause) {
        super(cause);
    }
}
