package de.fearnixx.t3.event.query;

import de.fearnixx.t3.ts3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.query.IQueryRequest;
import de.fearnixx.t3.ts3.query.IQueryMessage;
import de.fearnixx.t3.ts3.query.QueryConnection;

import java.util.List;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryEvent implements IQueryEvent {

    private QueryConnection conn;
    private IQueryMessage message;

    public QueryEvent(QueryConnection conn) {
        this.conn = conn;
    }

    public void setMessage(IQueryMessage message) {
        this.message = message;
    }

    @Override
    public IQueryMessageObject.IError getError() {
        return message.getError();
    }

    @Override
    public List<IQueryMessageObject> getObjects() {
        return message.getObjects();
    }

    @Override
    public QueryConnection getConnection() {
        return conn;
    }

    public static class Answer extends QueryEvent implements IAnswer {

        private IQueryRequest req;

        public Answer(QueryConnection conn, IQueryRequest req) {
            super(conn);
            this.req = req;
        }

        @Override
        public IQueryRequest getRequest() {
            return req;
        }
    }
}
