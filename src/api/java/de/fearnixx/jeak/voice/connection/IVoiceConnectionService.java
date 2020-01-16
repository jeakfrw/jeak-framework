package de.fearnixx.jeak.voice.connection;

import java.util.Optional;

/**
 * Service to provide voice connections
 */
public interface IVoiceConnectionService {

    /**
     * Returns the voice connection with the given identifier, if and only if it currently available.
     * Being available means that a connection is not used by another accessor of the service.
     * <p>
     * <p>
     * If the given identifier is not known, a new voice connection with its very own identity is created.
     * It will be accessible by this identifier in the future.
     *
     * @param identifier identifier of the connection
     * @return the voice connection, if available
     */
    Optional<IVoiceConnection> getVoiceConnection(String identifier);
}
