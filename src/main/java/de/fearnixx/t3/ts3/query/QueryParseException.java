package de.fearnixx.t3.ts3.query;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryParseException extends Exception {

    public QueryParseException(String msg) {
        super(msg);
    }

    public QueryParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
