package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.teamspeak.data.IDataHolder;

/**
 * Created by MarkL4YG on 10.06.17.
 *
 * @author Magnus Leßmann
 * @since 1.0.0
 * @deprecated In favor of {@link ITSQueryConnection} which more appropriately handles different connection channels.
 */
@Deprecated(since = "1.2.0")
public interface IQueryConnection extends AutoCloseable {

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
     * Whether or not this connection has been closed for any reason.
     */
    boolean isClosed();
}
