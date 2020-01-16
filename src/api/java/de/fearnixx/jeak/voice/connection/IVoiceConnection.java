package de.fearnixx.jeak.voice.connection;

import de.fearnixx.jeak.voice.sound.AudioType;
import de.fearnixx.jeak.voice.sound.IAudioPlayer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Represents a voice connection.
 *
 * A VoiceConnection may be requested by using {@link IVoiceConnectionService#getVoiceConnection(String)}.
 *
 * The retrieved connection will be not connected to the server, but it will already own a valid
 * teamspeak identity and can be connected to the server by calling {@link #connect()}.
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
     * Registers an Audio-Player with the respective type for the client-connection if the client is connected
     *
     * @return the audio player
     */
    IAudioPlayer registerAudioPlayer(AudioType audioType);
}
