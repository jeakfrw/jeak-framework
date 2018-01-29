package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.QueryEvent;

import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class RequestContainer {

    public Consumer<QueryEvent.Message.Answer> onDone;
    public IQueryRequest request;
}
