package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IQueryEvent.IAnswer;

import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contract held by any query connection established by the framework.
 *
 * @author Magnus Leßmann
 * @see de.fearnixx.jeak.teamspeak.query.IQueryConnection for the previous, deprecated contract.
 * @since 1.2.0
 */
public interface ITSQueryConnection extends AutoCloseable {

    /**
     * Queues a request for submission to the server.
     *
     * @apiNote Please note that request callbacks are <em>not invoked</em> by the connection. They must be invoked by {@link #onAnswer(Consumer)} listeners.
     * @implNote Queued requests may be rejected by the connection (e.g. when the connection closes). The answer will contain a negative error code in that case.
     */
    void queueRequest(IQueryRequest queryRequest);

    /**
     * Queues a request for submission to the server.
     * Returns a promise that is completed once an answer is available.
     *
     * @apiNote Please note that request callbacks are <em>not invoked</em> by the connection. They must be invoked by {@link #onAnswer(Consumer)} listeners.
     * @implNote Queued requests may be rejected by the connection (e.g. when the connection closes). The answer will contain a negative error code in that case.
     */
    Future<IAnswer> promiseRequest(IQueryRequest queryRequest);

    /**
     * Registers a listener to this connection which is fired when a notification has been received from the server.
     *
     * @implNote <em>Listeners are called synchronous to the reader-thread.</em> Please make sure to dispatch tasks to other threads!
     */
    void onNotification(Consumer<IQueryEvent.INotification> notificationConsumer);

    /**
     * Registers a listener to this connection which is fired when an answer has been received from the server.
     *
     * @implNote <em>Listeners are called synchronous to the reader-thread.</em> Please make sure to dispatch tasks to other threads!
     */
    void onAnswer(Consumer<IQueryEvent.IAnswer> answerConsumer);

    /**
     * Registers a listener to this connection which is fired when the connection has been closed.
     *
     * @apiNote The second parameter is an indicator for whether or not a graceful shutdown was used.
     * @implNote <em>Listeners are called synchronous to the reader-thread.</em> Please make sure to dispatch tasks to other threads!
     */
    void onClosed(BiConsumer<ITSQueryConnection, Boolean> closeConsumer);

    /**
     * Locks the methods {@link #onNotification(Consumer)}, {@link #onAnswer(Consumer)} and {@link #onClosed(BiConsumer)}.
     * Causes an {@link IllegalStateException} to be thrown by those methods with the provided reason being the exception message.
     *
     * @throws IllegalStateException when listeners are already locked.
     * @implNote By design, this is not reversible.
     */
    void lockListeners(String reason);

    /**
     * Gracefully closes this connection.
     * This should be preferred to {@link #close()}
     */
    void shutdown();

    /**
     * Forcefully closes this connection.
     * {@link #shutdown()} should be preferred for normal closes.
     *
     * @throws Exception When the underlying {@link AutoCloseable}s threw an exception.
     */
    @Override
    void close() throws Exception;
}
