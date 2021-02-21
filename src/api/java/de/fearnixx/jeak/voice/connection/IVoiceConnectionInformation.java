package de.fearnixx.jeak.voice.connection;


import java.util.Optional;

/**
 * Represents information of a voice connection
 */
public interface IVoiceConnectionInformation {

    /**
     * @return UNIQUE identifier of the connection
     */
    String getIdentifier();

    /**
     * @return nickname of the client
     */
    String getClientNickname();

    /**
     * @return description of the client
     */
    String getClientDescription();

    /**
     * @return the client unique id of the voice connection.
     */
    String getClientUniqueId();

    /**
     * @return the client id of the voice connection, if it is connected
     */
    Optional<Integer> optClientId();

    /**
     * @return the client id of the voice connection.
     * <p>
     * @throws IllegalStateException when trying to access the client id for a voice connection that is not connected.
     */
    Integer getClientId();
}
