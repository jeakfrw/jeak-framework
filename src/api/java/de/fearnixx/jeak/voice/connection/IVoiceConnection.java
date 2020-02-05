package de.fearnixx.jeak.voice.connection;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.voice.sound.AudioType;
import de.fearnixx.jeak.voice.sound.IAudioPlayer;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a voice connection.
 * <p>
 * A VoiceConnection may be requested by using {@link IVoiceConnectionService#requestVoiceConnection(String, Consumer)}.
 * <p>
 * The retrieved connection will be not connected to the server, but it will already own a valid
 * teamspeak identity and can be connected to the server by calling {@link #connect(Runnable, Runnable)}.
 */
public interface IVoiceConnection {

    /**
     * Connects the client to the server. Depending on the result of the connection process
     * the respective callback will be executed.
     * <p>
     * If the connection is already connected to the server no callback will be executed since there is no
     * result of a non-existing operation!
     */
    void connect(Runnable onSuccess, Runnable onError);

    /**
     * Disconnects the client from the server. Any timeouts while disconnecting are getting ignored.
     */
    void disconnect();

    /**
     * Disconnects the client from the server with a reason. Any timeouts while disconnecting are getting ignored.
     *
     * @param reason reason of the disconnect
     */
    void disconnect(String reason);

    /**
     * @return whether the client connection is connected to the server
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

    /**
     * @return the audio player that was registered for the voice connection using {@link #registerAudioPlayer(AudioType)}
     */
    Optional<IAudioPlayer> getRegisteredAudioPlayer();

    /**
     * @return information regarding the voice connection
     */
    IVoiceConnectionInformation getVoiceConnectionInformation();

    /**
     * Alters the voice connection information of the voice connection and sets the nickname of the client.
     *
     * @param nickname new nickname (may equal the old, but not null or an empty string)
     */
    void setClientNickname(String nickname);

    /**
     * Sets a flag for this voice connection whether incoming text messages should be forwarded
     * as {@link IQueryEvent.INotification.ITextMessage} to the framework to mimic text messages
     * that have been sent via the query connection
     *
     * @param shouldForwardTextMessages whether incoming text messages should be forwarded
     */
    void setShouldForwardTextMessages(boolean shouldForwardTextMessages);
}
