package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.Optional;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IServer {

    /**
     * Connect with the last connection details.
     */
    void connect();

    /**
     * Provides access to the main query connection.
     * @throws java.util.NoSuchElementException when the server is not connected.
     * * @implNote identity may change through reconnects! Do not store reference.
     * * @implNote in event-listeners, it is generally safe to assume that the connection is active
     */
    IQueryConnection getConnection();

    /**
     * @see #getConnection()
     */
    Optional<IQueryConnection> optConnection();

    /**
     * Whether or not an active connection is available.
     */
    boolean isConnected();

    /**
     * Returns a {@link IQueryRequest} that can be used to send a server-wide message
     */
    IQueryRequest sendMessage(String message);

    /**
     * The hostname used to connect to the server.
     */
    String getHost();

    /**
     * The query port of the server.
     */
    int getPort();

    /**
     * The server instance ID.
     */
    int getInstanceId();

    /**
     * The desired nickname for "clientupdate".
     * @apiNote This may be different from {@link IQueryConnection#getWhoAmI()}s "client_nickname" which is the actual nickname.
     */
    String getNickname();
}
