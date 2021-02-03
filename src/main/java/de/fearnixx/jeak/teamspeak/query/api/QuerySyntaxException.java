package de.fearnixx.jeak.teamspeak.query.api;

public class QuerySyntaxException extends Exception {

    public QuerySyntaxException() {
    }

    public QuerySyntaxException(String message) {
        super(message);
    }

    public QuerySyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuerySyntaxException(Throwable cause) {
        super(cause);
    }

    public QuerySyntaxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
