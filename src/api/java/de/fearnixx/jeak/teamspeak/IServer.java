package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.Optional;

/**
 * Representation of a virtual server to which Jeak will connect or is connected.
 */
public interface IServer {

    /**
     * Connect with the last connection details.
     *
     * @throws de.fearnixx.jeak.teamspeak.except.QueryConnectException when the connection failed.
     */
    void connect();

    /**
     * Provides access to the main query connection which is managed by Jeak.
     *
     * @throws java.util.NoSuchElementException when the server is not connected.
     * @implNote Identity may change through reconnects! Do not store reference. In event-listeners, it is generally safe to assume that the connection is active.
     */
    IQueryConnection getConnection();

    /**
     * Provides access to the main query connection.
     * Does not throw when the connection is inactive but will provide an empty optional instead.
     */
    Optional<IQueryConnection> optConnection();

    /**
     * Whether or not an active connection is available.
     */
    boolean isConnected();

    /**
     * Returns a {@link IQueryRequest} that can be used to send a server-wide message
     *
     * @deprecated In favor of {@link #broadcastTextMessage(String)} since its naming is more precise.
     */
    @Deprecated(since = "1.2.0")
    IQueryRequest sendMessage(String message);

    /**
     * Returns a {@link IQueryRequest} that can be used to broadcast a server-wide message, when sent through a connection.
     */
    IQueryRequest broadcastTextMessage(String message);

    /**
     * The hostname used to connect to the server.
     */
    String getHost();

    /**
     * The query port of the server.
     */
    int getPort();

    /**
     * The voice port of the server instance.
     *
     * @throws java.util.NoSuchElementException when the query connection is not established.
     */
    int getVoicePort();

    /**
     * The voice port of the server instance.
     */
    Optional<Integer> optVoicePort();

    /**
     * Response object of the <em>serverinfo</em> command retrieved from last execution.
     *
     * @see PropertyKeys.ServerInfo for the properties available.
     */
    Optional<IDataHolder> optServerInfoResponse();

    /**
     * The server instance ID.
     */
    int getInstanceId();

    /**
     * The desired nickname for "clientupdate".
     *
     * @apiNote This may be different from {@link IQueryConnection#getWhoAmI()}s "client_nickname" which is the actual nickname.
     * @deprecated This is a connection-specific internal detail and is misplaced in the server API.
     */
    @Deprecated(since = "1.2.0")
    String getNickname();
}
