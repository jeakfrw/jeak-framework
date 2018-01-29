package de.fearnixx.t3.teamspeak.query;

import java.io.IOException;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public class QueryConnectException extends IOException {

    public QueryConnectException(String msg) {
        super(msg);
    }

    public QueryConnectException(String msg, IOException e) {
        super(msg, e);
    }
}
