package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;

import java.util.concurrent.Future;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public interface IQueryPromise extends Future<IRawQueryEvent.IMessage.IAnswer> {

    IRawQueryEvent.IMessage.IAnswer get();
}
