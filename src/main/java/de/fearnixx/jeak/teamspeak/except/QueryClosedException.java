package de.fearnixx.jeak.teamspeak.except;

public class QueryClosedException extends QueryException {

    public QueryClosedException(String message) {
        super(message);
    }

    public QueryClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
