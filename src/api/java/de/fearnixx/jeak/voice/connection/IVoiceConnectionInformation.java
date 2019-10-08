package de.fearnixx.jeak.voice.connection;


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
}
