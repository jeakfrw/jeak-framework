package de.fearnixx.jeak.voice.connection;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service to provide {@link IVoiceConnection}
 */
public interface IVoiceConnectionService {

    /**
     * Requests the {@link IVoiceConnection} with the given identifier. Requesting corresponds to making the
     * voice connection with this identifier available to connect to the server at any time.
     * <p>
     * If the given identifier is not known, a new voice connection with its very own identity is created.
     * It will be accessible by this identifier in the future.
     * <p>
     * The provided callback will contain the requested voice connection if no error occurred during the request
     * or identity creation. The voice connection will NOT be connected to the server.
     *
     * @param identifier        identifier of the connection
     * @param onRequestFinished callback to handle the requested voice connection
     */
    void requestVoiceConnection(String identifier, Consumer<Optional<IVoiceConnection>> onRequestFinished);
}
