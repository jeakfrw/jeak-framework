package de.fearnixx.jeak.teamspeak.voice.connection;

import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.event.TS3Listener;
import com.github.manevolent.ts3j.event.TextMessageEvent;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.voice.connection.event.VoiceConnectionTextMessageEvent;
import de.fearnixx.jeak.teamspeak.voice.connection.info.AbstractVoiceConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.sound.Mp3AudioPlayer;
import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.sound.IMp3AudioPlayer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class VoiceConnection implements IVoiceConnection {

    private final AbstractVoiceConnectionInformation clientConnectionInformation;
    private final String hostname;
    private final int port;
    private final IEventService eventService;

    private LocalTeamspeakClientSocket ts3jClientSocket;
    private boolean locked;

    private boolean connected;

    VoiceConnection(AbstractVoiceConnectionInformation clientConnectionInformation, String hostname, int port, IEventService eventService) {
        this.clientConnectionInformation = clientConnectionInformation;
        this.hostname = hostname;
        this.port = port;
        this.eventService = eventService;
        locked = true;
    }

    @Override
    public void connect() throws IOException, TimeoutException {
        this.ts3jClientSocket = new LocalTeamspeakClientSocket();

        ts3jClientSocket.setNickname(clientConnectionInformation.getClientNickname());
        ts3jClientSocket.setIdentity(clientConnectionInformation.getTeamspeakIdentity());

        ts3jClientSocket.addListener(
                new TS3Listener() {
                    @Override
                    public void onTextMessage(TextMessageEvent e) {
                        eventService.fireEvent(
                                new VoiceConnectionTextMessageEvent(clientConnectionInformation.getIdentifier(), e)
                        );
                    }
                }
        );

        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        ts3jClientSocket.connect(inetSocketAddress, null, 10000L);

        this.connected = true;
    }

    @Override
    public void disconnect() {
        disconnect(null);
    }

    @Override
    public void disconnect(String reason) {
        try {
            if (ts3jClientSocket.getMicrophone() != null
                    && ts3jClientSocket.getMicrophone().getClass().isAssignableFrom(Mp3AudioPlayer.class)) {
                ((Mp3AudioPlayer) ts3jClientSocket.getMicrophone()).stop();
            }

            ts3jClientSocket.disconnect(reason);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (IOException | ExecutionException | TimeoutException e) {
            //We assume that the server is not reachable for this connection. Therefor it is declared disconnected
        }

        locked = false;
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void sendToChannel(int channelId) {
        sendToChannel(channelId, null);
    }

    @Override
    public void sendToChannel(int channelId, String password) {
        try {
            ts3jClientSocket.clientMove(ts3jClientSocket.getClientId(), channelId, password);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (IOException | TimeoutException | CommandException e) {
            //The client movement failed

            //TODO: return the result of an action performed on the connection
        }
    }

    @Override
    public IMp3AudioPlayer registerMp3AudioPlayer() {
        if (!connected) {
            throw new IllegalStateException("An Mp3AudioPlayer can only be registered when the client connection is connected");
        }

        Mp3AudioPlayer mp3AudioPlayer = new Mp3AudioPlayer();
        ts3jClientSocket.setMicrophone(mp3AudioPlayer);

        return mp3AudioPlayer;
    }

    boolean isLocked() {
        return locked;
    }
}
