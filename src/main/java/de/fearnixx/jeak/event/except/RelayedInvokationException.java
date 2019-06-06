package de.fearnixx.jeak.event.except;

public class RelayedInvokationException extends RuntimeException {

    public RelayedInvokationException() {
        super();
    }

    public RelayedInvokationException(String message) {
        super(message);
    }

    public RelayedInvokationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RelayedInvokationException(Throwable cause) {
        super(cause);
    }

    protected RelayedInvokationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
