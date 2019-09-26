package de.fearnixx.jeak.voice.connection;

/**
 * Service to provide and handle client connection
 */
public interface IClientConnectionService {

    /**
     * Checks whether the client connection with the given identifier is available
     *
     * @param identifier identifier of the connection
     * @return if the client connection is available
     */
    boolean isConnectionAvailable(String identifier);

    /**
     * Returns the client connection with the given identifier.
     * The client connection has to be available.
     *
     * @param identifier identifier of the connection
     * @return the client connection
     */
    IClientConnection getClientConnection(String identifier);
}
