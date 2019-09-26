package de.fearnixx.jeak.voice.connection;


/**
 * Represents information on a connection of a client
 */
public interface IClientConnectionInformation {

    /**
     * @return UNIQUE identifier of the connection
     */
    String getIdentifier();

    /**
     * @return nickname of the client
     */
    String getClientNickname();
}
