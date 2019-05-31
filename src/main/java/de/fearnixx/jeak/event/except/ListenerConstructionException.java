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

    protected ListenerConstructionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
