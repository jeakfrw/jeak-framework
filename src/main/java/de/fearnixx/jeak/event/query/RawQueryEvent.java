package de.fearnixx.jeak.event.query;

import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.ITSQueryConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class RawQueryEvent extends BasicDataHolder implements IRawQueryEvent {

    private ITSQueryConnection tsConnection;

    public IQueryConnection getConnection() {
        return (IQueryConnection) tsConnection;
    }

    public void setConnection(IQueryConnection queryConnection) {
        this.tsConnection = (ITSQueryConnection) queryConnection;
    }

    public void setTsConnection(ITSQueryConnection tsConnection) {
        this.tsConnection = tsConnection;
    }

    public ITSQueryConnection getTsConnection() {
        return tsConnection;
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
            while (msg != null) {
                msgs.add(msg);
                msg = msg.getNext();
            }
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

            private final IQueryRequest request;

            public Answer(IQueryRequest request) {
                this.request = request;
            }

            public IQueryRequest getRequest() {
                return request;
            }
        }

        public static class Notification extends Message implements IRawQueryEvent.IMessage.INotification {

            private String caption;
            private Integer hash;

            public Notification() {
                setError(ErrorMessage.OK());
            }

            public String getCaption() {
                return caption;
            }

            public void setCaption(String caption) {
                this.caption = caption;
            }

            public void setHashCode(Integer hash) {
                this.hash = hash;
            }

            public int getHashCode() {
                return hash;
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
