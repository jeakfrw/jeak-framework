package de.fearnixx.jeak.voice.connection;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Gives access to a pool of {@link IVoiceConnection}. All connections that have been requested using this
 * pool ({@link #registerVoiceConnection(String)}) can be accessed directly.
 */
public interface IVoiceConnectionPool {

    /**
     * Registers a voice connection in this pool. It will be immediately accessible
     * after a successful request registration.
     * <p>
     * No callbacks will be executed.
     *
     * @param identifier unique identifier of the voice connection
     */
    void registerVoiceConnection(String identifier);


    /**
     * Registers a voice connection in this pool. It will be immediately accessible
     * after a successful request registration.
     *
     * <p>
     * The supplied callback will be executed if - and only if - the {@link IVoiceConnectionPool}
     * has been added to the pool. (Therefore, a failed registration will NOT call any callback)
     *
     * @param identifier unique identifier of the voice connection
     */
    void registerVoiceConnection(String identifier, Consumer<IVoiceConnection> onRegistrationFinished);

    /**
     * Returns the voice connection, if and only if it was requested using this pool. Any
     * accesses to a connection that was not requested or which creation failed will result
     * in an {@link IllegalArgumentException}.
     *
     * @param identifier unique identifier of the voice connection
     * @return the voice connection with the given identifier
     */
    IVoiceConnection getVoiceConnection(String identifier);

    /**
     * Returns the voice connection with the given identifier, if it was registered in this pool.
     *
     * @param identifier unique identifier of the voice connection
     * @return the voice connection, if it was registered successfully
     */
    Optional<IVoiceConnection> optVoiceConnection(String identifier);
}
