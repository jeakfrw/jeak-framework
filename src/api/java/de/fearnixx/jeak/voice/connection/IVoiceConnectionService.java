package de.fearnixx.jeak.voice.connection;

import java.util.function.Consumer;

/**
 * Service to provide {@link IVoiceConnection}
 */
public interface IVoiceConnectionService {

    /**
     * Requests a {@link IVoiceConnection} with the given identifier.
     * <p>
     * If the given identifier is not known, a new voice connection with its very own identity is created.
     * It will be accessible by this identifier in the future.
     * <p>
     * Voice connections that have already been requested are retrieved from an internal cache.
     * <p>
     * The provided callback will be executed using the requested voice connection.
     * <p>
     * The voice connection might be connected to the server if it was requested and connected earlier.
     * After the first request, a voice connection is not connected to the server.
     *
     * @param identifier        identifier of the connection
     * @param onRequestFinished callback to handle the requested voice connection
     */
    void requestVoiceConnection(String identifier, Consumer<IVoiceConnection> onRequestFinished);

    /**
     * Creates a new {@link IVoiceConnectionStore} which will be using this service for requesting {@link IVoiceConnection}.
     *
     * @return a new pool
     */
    IVoiceConnectionStore createVoiceConnectionStore();

    /**
     * Creates a new {@link IVoiceConnectionStore} which will be using this service for requesting
     * {@link IVoiceConnection}. For given identifiers a voice connection will be prepared for the store.
     *
     * @param identifiers all identifiers to prepare voice connections for. Must contain only unique values.
     * @return a new pool with prepared voice connections for every identifier
     */
    IVoiceConnectionStore createVoiceConnectionStore(String... identifiers);
}
