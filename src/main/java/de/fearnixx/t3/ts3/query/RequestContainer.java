package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.event.query.IQueryEvent;

import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class RequestContainer {

    public Consumer<IQueryEvent.IAnswer> onDone;
    public IQueryRequest request;

}
