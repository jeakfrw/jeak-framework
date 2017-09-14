package de.fearnixx.t3.event.query;

import de.fearnixx.t3.ts3.query.IQueryRequest;
import de.fearnixx.t3.ts3.query.IQueryMessage;
import de.fearnixx.t3.ts3.query.QueryConnection;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryEvent implements IQueryEvent {

    private QueryConnection conn;

    public QueryEvent(QueryConnection conn) {
        this.conn = conn;
    }

    @Override
    public QueryConnection getConnection() {
        return conn;
    }

    public static class Message extends QueryEvent implements IMessage {

        private IQueryRequest req;
        private IQueryMessage msg;

        public Message(QueryConnection conn, IQueryRequest req, IQueryMessage msg) {
            super(conn);
            this.req = req;
            this.msg = msg;
        }

        @Override
        public IQueryMessage getMessage() {
            return msg;
        }

        @Override
        public IQueryRequest getRequest() {
            return req;
        }
    }
}
