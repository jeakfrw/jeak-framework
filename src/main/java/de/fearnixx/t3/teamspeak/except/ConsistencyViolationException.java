package de.fearnixx.t3.teamspeak.except;

public class ConsistencyViolationException extends RuntimeException {

    public ConsistencyViolationException() {
    }

    public ConsistencyViolationException(String message) {
        super(message);
    }

    public ConsistencyViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsistencyViolationException(Throwable cause) {
        super(cause);
    }

    public ConsistencyViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
