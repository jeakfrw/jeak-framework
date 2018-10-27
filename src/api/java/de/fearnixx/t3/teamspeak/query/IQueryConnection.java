package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.teamspeak.data.IDataHolder;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public interface IQueryConnection {

    /**
     * Sets the nickname of this server query connection
     * @param nickName The new nickname
     */
    void setNickName(String nickName);

    /**
     * @return The last response to the "whoami" command - contains sometimes useful information
     */
    IDataHolder getWhoAmI();

    /**
     * Send a request.
     *
     * This queues the request and sends it when possible.
     * @param req The request to send
     */
    void sendRequest(IQueryRequest req);

    /**
     * Send a request
     *
     * This queues the request and sends it when possible.
     * The callback will be called when a full answer has been received.
     * @param req The request
     * @param onDone The callback consumer
     *
     * @deprecated Replaced by {@link IQueryRequest#onDone()} but not immediately deactivated.
     *             For removal by RC#1!
     */
    @Deprecated
    void sendRequest(IQueryRequest req, Consumer<IRawQueryEvent.IMessage.IAnswer> onDone);

    /**
     * Send a request
     *
     * This queues the request and sends it when possible.
     * The callback will be called when a full answer has been received.
     *
     * @deprecated Convention-Replaced by {@link IQueryRequest#onDone()}. No requests should block framework threads!
     */
    @Deprecated
    IQueryPromise promiseRequest(IQueryRequest request);
}
