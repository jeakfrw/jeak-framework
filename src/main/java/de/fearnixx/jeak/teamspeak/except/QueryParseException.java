package de.fearnixx.jeak.teamspeak.except;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryParseException extends QueryException {

    public QueryParseException(String msg) {
        super(msg);
    }

    public QueryParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
