package de.fearnixx.jeak.voice.connection;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Gives access to a store of {@link IVoiceConnection}. All connections that have been requested using this
 * store ({@link #prepareVoiceConnection(String)}) can be accessed directly.
 */
public interface IVoiceConnectionStore {

    /**
     * Registers a voice connection in this store. It will be immediately accessible
     * after a successful request registration.
     * <p>
     * No callbacks will be executed.
     *
     * @param identifier unique identifier of the voice connection
     */
    void prepareVoiceConnection(String identifier);


    /**
     * Registers a voice connection in this store. It will be immediately accessible
     * after a successful registration request.
     *
     * <p>
     * The supplied callback will be executed if - and only if - the {@link IVoiceConnectionStore}
     * has been added to the store. (Therefore, a failed registration will NOT call any callback)
     *
     * @param identifier unique identifier of the voice connection
     */
    void prepareVoiceConnection(String identifier, Consumer<IVoiceConnection> onRegistrationFinished);

    /**
     * Returns the voice connection, if and only if it was requested using this store. Any
     * accesses to a connection that was not requested or which creation failed will result
     * in an {@link IllegalArgumentException}.
     *
     * @param identifier unique identifier of the voice connection
     * @return the voice connection with the given identifier
     */
    IVoiceConnection getVoiceConnection(String identifier);

    /**
     * Returns the voice connection with the given identifier, if it was registered in this store.
     *
     * @param identifier unique identifier of the voice connection
     * @return the voice connection, if it was registered successfully
     */
    Optional<IVoiceConnection> optVoiceConnection(String identifier);
}
