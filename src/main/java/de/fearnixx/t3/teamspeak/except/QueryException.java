package de.fearnixx.t3.teamspeak.except;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public class QueryException extends RuntimeException {

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
