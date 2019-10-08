package de.fearnixx.jeak.voice.connection;

/**
 * Service to provide voice connections
 */
public interface IVoiceConnectionService {

    /**
     * Checks whether the voice connection with the given identifier is available
     *
     * @param identifier identifier of the connection
     * @return if the voice connection is available
     */
    boolean isConnectionAvailable(String identifier);

    /**
     * Returns the voice connection with the given identifier.
     * The voice connection has to be available.
     *
     * @param identifier identifier of the connection
     * @return the voice connection
     */
    IVoiceConnection getVoiceConnection(String identifier);
}
