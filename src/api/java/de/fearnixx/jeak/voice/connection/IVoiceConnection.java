package de.fearnixx.jeak.voice.connection;

import de.fearnixx.jeak.voice.sound.IMp3AudioPlayer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Represents a voice connection
 */
public interface IVoiceConnection {

    /**
     * Connects the client to the server
     *
     * @throws IOException      If the server hostname is invalid or cant be resolved
     * @throws TimeoutException on a timeout
     */
    void connect() throws IOException, TimeoutException;

    /**
     * Disconnects the client from the server
     *
     * @throws TimeoutException on a timeout
     */
    void disconnect() throws TimeoutException;

    /**
     * Disconnects the client from the server with a reason
     *
     * @param reason reason of the disconnect
     * @throws TimeoutException on a timeout
     */
    void disconnect(String reason) throws TimeoutException;

    /**
     * Check whether the client connection is connected to the server
     *
     * @return connected or not
     */
    boolean isConnected();

    /**
     * Sends the client to the given channel
     *
     * @param channelId id of the desired channel
     */
    void sendToChannel(int channelId);

    /**
     * Sends the client to the given channel
     *
     * @param channelId id of the desired channel
     * @param password  password of the channel
     */
    void sendToChannel(int channelId, String password);

    /**
     * Registers an Mp3-Audio-Player for the client-connection if the client is connected
     *
     * @return mp3AudioPlayer the audio player
     */
    IMp3AudioPlayer registerMp3AudioPlayer();
}
