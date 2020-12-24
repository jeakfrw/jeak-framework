package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.ITSQueryConnection;

import java.util.Optional;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IServer {

    /**
     * Connect with the last connection details.
     *
     * @throws de.fearnixx.jeak.teamspeak.except.QueryConnectException when the connection failed.
     */
    void connect();

    /**
     * Provides access to the main query connection.
     *
     * @throws java.util.NoSuchElementException when the server is not connected.
     * @implNote identity may change through reconnects! Do not store reference. In event-listeners, it is generally safe to assume that the connection is active
     * @deprecated Affected by deprecation of {@link IQueryConnection}. Use {@link #getQueryConnection()} instead.
     */
    @Deprecated(since = "1.2.0")
    IQueryConnection getConnection();

    /**
     * @see #getConnection()
     * @deprecated Affected by deprecation of {@link IQueryConnection}. Use {@link #optQueryConnection()} instead.
     */
    @Deprecated(since = "1.2.0")
    Optional<IQueryConnection> optConnection();

    /**
     * Provides access to the main query connection.
     *
     * @throws IllegalStateException when the server is not connected.
     * @apiNote Identity will change through reconnects. Do <strong>not</strong> store this reference.
     * @implNote In event-listeners, it is generally safe to assume the connection is active.
     */
    ITSQueryConnection getQueryConnection();

    /**
     * Provides access to the main query connection.
     * Returned value will be {@link Optional#empty()} when the connection is currently offline.
     *
     * @apiNote Identity will change through reconnects. Do <strong>not</strong> store this reference.
     * @implNote In event-listeners, it is generally safe to assume the connection is active.
     */
    Optional<ITSQueryConnection> optQueryConnection();

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
     */
    String getNickname();
}
