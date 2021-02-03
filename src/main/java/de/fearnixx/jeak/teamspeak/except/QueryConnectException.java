package de.fearnixx.jeak.teamspeak.except;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public class QueryConnectException extends QueryException {

    public QueryConnectException(String msg) {
        super(msg);
    }

    public QueryConnectException(String msg, Exception e) {
        super(msg, e);
    }
}
