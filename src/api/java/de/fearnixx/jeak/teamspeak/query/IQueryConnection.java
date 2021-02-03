package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

import java.util.concurrent.Future;

/**
 * API contract of the main query connection managed by the framework.
 *
 * @author Magnus Leßmann
 * @apiNote This builds on top of the internal library
 * @since 1.0.0
 */
public interface IQueryConnection extends AutoCloseable {

    /**
     * Sets the nickname of this server query connection.
     *
     * @param nickName The new nickname
     * @apiNote This is queued just as other requests and may not be in effect immediately.
     */
    void setNickName(String nickName);

    /**
     * @return The last response to the "whoami" command - contains sometimes useful information
     */
    IDataHolder getWhoAmI();

    /**
     * Send a request.
     * <p>
     * This queues the request and sends it when possible.
     *
     * @param req The request to send
     * @deprecated In favor of the more fittingly named {@link #queueRequest(IQueryRequest)}
     */
    void sendRequest(IQueryRequest req);

    /**
     * Queues a request for submission to the server.
     *
     * @apiNote The framework will invoke request callbacks asynchronously.
     * @implNote Queued requests may be rejected by the connection (e.g. when the connection closes). The answer will contain a negative error code as an indicator.
     * @since 1.2.0
     */
    void queueRequest(IQueryRequest request);

    /**
     * Queues a request for submission to the server.
     * Returns a promise that is completed once an answer is available.
     *
     * @apiNote The framework will invoke request callbacks asynchronously.
     * @implNote Queued requests may be rejected by the connection (e.g. when the connection closes). The answer will contain a negative error code as an indicator.
     * @since 1.2.0
     */
    Future<IQueryEvent.IAnswer> promiseRequest(IQueryRequest request);

    /**
     * Whether or not this connection has been closed for any reason.
     */
    boolean isClosed();
}
