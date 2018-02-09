package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;

import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class RequestContainer {

    public Consumer<IRawQueryEvent.IMessage.IAnswer> onDone;
    public IQueryRequest request;
}
