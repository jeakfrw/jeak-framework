package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;

import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class RequestContainer {

    public RequestContainer(Consumer<IRawQueryEvent.IMessage.IAnswer> onDone, IQueryRequest request) {
        this.onDone = onDone;
        this.request = request;
    }

    private Consumer<IRawQueryEvent.IMessage.IAnswer> onDone;
    private IQueryRequest request;

    public Consumer<IRawQueryEvent.IMessage.IAnswer> getOnDone() {
        return onDone;
    }

    public IQueryRequest getRequest() {
        return request;
    }
}
