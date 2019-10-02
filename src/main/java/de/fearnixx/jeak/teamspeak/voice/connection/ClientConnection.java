package de.fearnixx.jeak.teamspeak.voice.connection;

import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
import de.fearnixx.jeak.teamspeak.voice.connection.info.AbstractClientConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.sound.Mp3AudioPlayer;
import de.fearnixx.jeak.voice.connection.IClientConnection;
import de.fearnixx.jeak.voice.sound.IMp3AudioPlayer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ClientConnection implements IClientConnection {

    private final AbstractClientConnectionInformation clientConnectionInformation;
    private final String hostname;
    private final int port;

    private LocalTeamspeakClientSocket ts3jClientSocket;
    private boolean locked;

    private boolean connected;

    ClientConnection(AbstractClientConnectionInformation clientConnectionInformation, String hostname, int port) {
        this.clientConnectionInformation = clientConnectionInformation;
        this.hostname = hostname;
        this.port = port;
        locked = true;
    }

    @Override
    public void connect() throws IOException, TimeoutException {
        this.ts3jClientSocket = new LocalTeamspeakClientSocket();

        ts3jClientSocket.setNickname(clientConnectionInformation.getClientNickname());
        ts3jClientSocket.setIdentity(clientConnectionInformation.getTeamspeakIdentity());

        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        ts3jClientSocket.connect(inetSocketAddress, null, 10000L);

        this.connected = true;
    }

    @Override
    public void disconnect() throws TimeoutException {
        disconnect(null);
    }

    @Override
    public void disconnect(String reason) throws TimeoutException {
        try {
            if (ts3jClientSocket.getMicrophone() != null
                    && ts3jClientSocket.getMicrophone().getClass().isAssignableFrom(Mp3AudioPlayer.class)) {
                ((Mp3AudioPlayer) ts3jClientSocket.getMicrophone()).stop();
            }

            ts3jClientSocket.disconnect(reason);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (IOException | ExecutionException e) {
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
