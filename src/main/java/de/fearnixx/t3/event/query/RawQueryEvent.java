package de.fearnixx.t3.event.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.teamspeak.data.DataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.QueryConnection;

import java.util.*;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class RawQueryEvent extends DataHolder implements IRawQueryEvent {

    public QueryConnection connection;

    public void setConnection(QueryConnection connection) {
        this.connection = connection;
    }

    public QueryConnection getConnection() {
        return connection;
    }

    public static abstract class Message extends RawQueryEvent implements IRawQueryEvent.IMessage {

        private String command;
        protected ErrorMessage error;

        protected Message previous;
        protected Message next;

        public void setPrevious(RawQueryEvent.Message last) {
            this.previous = last;
        }

        public void setNext(Message next) {
            this.next = next;
        }

        public boolean hasPrevious() {
            return previous != null && previous != this;
        }

        public boolean hasNext() {
            return next != null && next != this;
        }

        public Message getPrevious() {
            return previous != this ? previous : null;
        }

        public Message getNext() {
            return next != this ? next : null;
        }

        public List<IMessage> toList() {
            List<IMessage> msgs = new ArrayList<>();
            Message msg = this;
            do {
                msgs.add(msg);
            } while ((msg = msg.getNext()) != null);
            return msgs;
        }

        public void setError(ErrorMessage message) {
            this.error = message;
            if (hasNext())
                getNext().setError(message);
        }

        public IErrorMessage getError() {
            return error;
        }

        public static class Answer extends Message implements IRawQueryEvent.IMessage.IAnswer {

            private IQueryRequest request;

            public Answer(IQueryRequest request) {
                this.request = request;
            }

            public IQueryRequest getRequest() {
                return request;
            }
        }

        public static class Notification extends Message implements IRawQueryEvent.IMessage.INotification {

            private String caption;

            public Notification() {
                setError(ErrorMessage.OK());
            }

            public String getCaption() {
                return caption;
            }

            public void setCaption(String caption) {
                this.caption = caption;
            }
        }
    }

    public static class ErrorMessage extends Message.Answer implements IRawQueryEvent.IMessage.IErrorMessage {

        public static ErrorMessage OK() {
            ErrorMessage m = new ErrorMessage(null);
            m.setProperty("command", "error");
            m.setProperty("id", "0");
            m.setProperty("msg", null);
            return m;
        }

        public ErrorMessage(IQueryRequest request) {
            super(request);
        }

        public Integer getCode() {
            return Integer.parseInt(getProperty("id").orElse("0"));
        }

        public String getMessage() {
            return getProperty("msg").orElse(null);
        }

        @Override
        public ErrorMessage getError() {
            return this;
        }
    }
}
