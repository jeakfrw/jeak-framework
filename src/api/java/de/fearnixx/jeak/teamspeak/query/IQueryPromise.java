package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IRawQueryEvent;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Plugins may block their thread for a short amount of time using this {@link Future} implementation.
 *
 * It is highly recommended to prefer using the callbacks of {@link IQueryRequest} or {@link IQueryConnection#sendRequest(IQueryRequest, Consumer)}
 *
 * @implNote {@link #get()} (without timeout) is <strong>not supported!</strong>
 */
public interface IQueryPromise extends Future<IRawQueryEvent.IMessage.IAnswer> {
}
