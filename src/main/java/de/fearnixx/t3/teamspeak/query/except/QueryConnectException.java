package de.fearnixx.t3.teamspeak.query.except;

import java.io.IOException;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public class QueryConnectException extends QueryException {

    public QueryConnectException(String msg) {
        super(msg);
    }

    public QueryConnectException(String msg, IOException e) {
        super(msg, e);
    }
}
