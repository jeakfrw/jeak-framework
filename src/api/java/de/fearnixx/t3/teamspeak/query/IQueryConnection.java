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
     * Issues a query login.
     *
     * Sends the commands "login" and "use"
     *
     * Blocks the thread until the answers are available
     * @param instanceID The desired virtualserver instance ID (0 for none)
     * @param user The user to login with
     * @param pass The password to login with
     * @return Whether or not all commands succeeded
     */
    boolean blockingLogin(Integer instanceID, String user, String pass);

    /**
     * @return The currently selected server instance ID
     */
    Integer getInstanceID();

    /**
     * Sets the nickname of this server query connection
     * @param newNickName The new nickname
     */
    void setNickName(String newNickName);

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
     * The callback will be called when a full answer has been received
     * @param req The request
     * @param onDone The callback consumer
     */
    void sendRequest(IQueryRequest req, Consumer<IRawQueryEvent.IMessage.IAnswer> onDone);

    /**
     * Send a request
     *
     * This queues the request and sends it when possible.
     * The callback will be called when a full answer has been received
     */
    Future<IRawQueryEvent.IMessage.IAnswer> promiseRequest(IQueryRequest request);
}
