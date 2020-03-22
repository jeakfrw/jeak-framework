package de.fearnixx.jeak.teamspeak.voice.connection;

import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.event.TS3Listener;
import com.github.manevolent.ts3j.event.TextMessageEvent;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.EventCaptions;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.voice.connection.event.VoiceConnectionTextMessageEvent;
import de.fearnixx.jeak.teamspeak.voice.connection.info.AbstractVoiceConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.sound.AudioPlayer;
import de.fearnixx.jeak.teamspeak.voice.sound.Mp3AudioPlayer;
import de.fearnixx.jeak.voice.connection.ConnectionFailure;
import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionInformation;
import de.fearnixx.jeak.voice.sound.AudioType;
import de.fearnixx.jeak.voice.sound.IAudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class VoiceConnection implements IVoiceConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceConnection.class);

    private final AbstractVoiceConnectionInformation clientConnectionInformation;
    private final String hostname;
    private final int port;
    private final IEventService eventService;

    private LocalTeamspeakClientSocket ts3jClientSocket;

    private boolean connected;

    private IUserService userService;
    private IDataCache cache;

    private IBot bot;

    private boolean shouldForwardTextMessages;

    VoiceConnection(AbstractVoiceConnectionInformation clientConnectionInformation, String hostname, int port, IEventService eventService, IBot bot, IUserService userService, IDataCache cache) {
        this.clientConnectionInformation = clientConnectionInformation;
        this.hostname = hostname;
        this.port = port;
        this.eventService = eventService;
        this.userService = userService;
        this.cache = cache;
        this.bot = bot;
    }

    @Override
    public synchronized void connect(Runnable onSuccess, Consumer<ConnectionFailure> onError) {
        if (connected) {
            LOGGER.warn(
                    "Tried to connect an already connected voice connection! Identifier: {}",
                    clientConnectionInformation.getIdentifier()
            );
            return;
        }

        Executors.newSingleThreadExecutor().execute(
                () -> {
                    this.ts3jClientSocket = new LocalTeamspeakClientSocket();

                    ts3jClientSocket.setNickname(clientConnectionInformation.getClientNickname());
                    ts3jClientSocket.setIdentity(clientConnectionInformation.getTeamspeakIdentity());

                    ts3jClientSocket.addListener(
                            new TS3Listener() {
                                @Override
                                public void onTextMessage(TextMessageEvent e) {
                                    handleTextMessageEvent(e);
                                }
                            }
                    );

                    InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
                    try {
                        final Long timeout = Main.getProperty("jeak.voice_connection.connection_timeout", 10000L);
                        ts3jClientSocket.connect(inetSocketAddress, null, timeout);
                    } catch (IOException e) {
                        LOGGER.error(
                                "An unspecified error occurred while trying to connect the voice connection with " +
                                        "the identifier {}!",
                                clientConnectionInformation.getIdentifier(), e
                        );
                        onError.accept(ConnectionFailure.UNSPECIFIED);
                        return;
                    } catch (TimeoutException e) {
                        LOGGER.error(
                                "A timeout occurred while trying to connect the voice connection with " +
                                        "the identifier {}!",
                                clientConnectionInformation.getIdentifier()
                        );
                        onError.accept(ConnectionFailure.TIMEOUT);
                        return;
                    }

                    this.connected = true;
                    clientConnectionInformation.setClientId(ts3jClientSocket.getClientId());

                    onSuccess.run();
                }
        );
    }

    private void handleTextMessageEvent(TextMessageEvent e) {
        final IQueryConnection connection;

        Optional<IQueryConnection> optConnection = bot.getServer().optConnection();
        if (optConnection.isEmpty()) {
            LOGGER.warn("The voice connection with the identifier {} received a " +
                    "text message that should be forwarded, but the query connection " +
                    "was not available!", clientConnectionInformation.getIdentifier()
            );
            return;
        }

        connection = optConnection.get();

        eventService.fireEvent(
                new VoiceConnectionTextMessageEvent(
                        clientConnectionInformation.getIdentifier(), e
                )
        );

        if (shouldForwardTextMessages) {
            final QueryEvent.Notification textMessageEvent;
            final int invokerId = e.getInvokerId();
            final IClient client = userService.getClientByID(invokerId).orElseThrow();

            switch (e.getTargetMode()) {
                case CLIENT:
                    QueryEvent.ClientTextMessage clientTextMessage =
                            new QueryEvent.ClientTextMessage(userService);

                    clientTextMessage.setClient(client);
                    textMessageEvent = clientTextMessage;
                    break;
                case CHANNEL:
                    LOGGER.info(
                            "The voice connection with the identifier {} received a ChannelTextMessageEvent," +
                                    " which will not be forwarded!",
                            getVoiceConnectionInformation().getIdentifier()
                    );
                    //This feature might be included in a future version and probably will be an own event listener
                    return;
                case SERVER:
                    //Server text messages are already handled hence no forwarding is required
                    return;
                default:
                    throw new IllegalStateException(
                            "Received text message event with unsupported target mode "
                                    + e.getTargetMode() + "!"
                    );
            }

            textMessageEvent.setCaption(EventCaptions.TEXT_MESSAGE);
            textMessageEvent.setConnection(connection);
            textMessageEvent.setProperty(PropertyKeys.TextMessage.MESSAGE, e.getMessage());
            textMessageEvent.setProperty(PropertyKeys.TextMessage.SOURCE_ID, invokerId);
            textMessageEvent.setProperty(
                    PropertyKeys.TextMessage.SOURCE_UID,
                    client.getClientUniqueID()
            );
            textMessageEvent.setProperty(
                    PropertyKeys.TextMessage.SOURCE_NICKNAME,
                    client.getNickName()
            );
            textMessageEvent.setProperty(
                    PropertyKeys.TextMessage.TARGET_TYPE, e.getTargetMode()
            );

            eventService.fireEvent(textMessageEvent);
        }
    }

    @Override
    public void disconnect() {
        disconnect(null);
    }

    @Override
    public synchronized void disconnect(String reason) {
        try {
            if (ts3jClientSocket.getMicrophone() != null
                    && ts3jClientSocket.getMicrophone().getClass().isAssignableFrom(IAudioPlayer.class)) {
                ((IAudioPlayer) ts3jClientSocket.getMicrophone()).stop();
            }

            ts3jClientSocket.disconnect(reason);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (IOException | ExecutionException | TimeoutException e) {
            //We assume that the server is not reachable for this connection. Therefor it is declared disconnected
            LOGGER.warn("An exception occurred while the voice connection with the identifier {} " +
                    "tried to disconnect from the server.", clientConnectionInformation.getIdentifier(), e
            );
        }

        connected = false;
    }

    @Override
    public synchronized boolean isConnected() {
        return connected;
    }

    @Override
    public boolean sendToChannel(int channelId) {
        return sendToChannel(channelId, null);
    }

    @Override
    public boolean sendToChannel(int channelId, String password) {
        try {
            ts3jClientSocket.clientMove(ts3jClientSocket.getClientId(), channelId, password);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            //The client movement failed due to an unspecified exception
            LOGGER.error("An unspecified exception occurred while the voice connection with the identifier {} " +
                            "tried to move to the channel with the id {}.", clientConnectionInformation.getIdentifier(),
                    channelId, e
            );

            return false;
        } catch (TimeoutException e) {
            //The client movement failed due to a timeout
            LOGGER.warn("A timeout occurred while the voice connection with the identifier {} " +
                            "tried to move to the channel with the id {}.", clientConnectionInformation.getIdentifier(),
                    channelId
            );

            return false;
        } catch (CommandException e) {
            //The client movement failed due to an error regarding teamspeak
            LOGGER.warn("An error occurred while the voice connection with the identifier {} " +
                            "tried to move to the channel with the id {}. Error code {}",
                    clientConnectionInformation.getIdentifier(),
                    channelId, e.getErrorId()
            );

            return false;
        }

        return true;
    }

    @Override
    public synchronized IAudioPlayer registerAudioPlayer(AudioType audioType) {
        if (!connected) {
            throw new IllegalStateException(
                    "An audio player can only be registered when the client connection is connected!"
            );
        }

        AudioPlayer audioPlayer;

        switch (audioType) {
            case MP3:
                audioPlayer = new Mp3AudioPlayer();
                break;
            default:
                throw new IllegalArgumentException(
                        "The audio type " + audioType.toString() + "is currently not supported!"
                );
        }

        ts3jClientSocket.setMicrophone(audioPlayer);

        return audioPlayer;
    }

    @Override
    public Optional<IAudioPlayer> optRegisteredAudioPlayer() {
        if (ts3jClientSocket == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(ts3jClientSocket.getMicrophone()).map(mic -> (IAudioPlayer) mic);
        }
    }

    @Override
    public IAudioPlayer getRegisteredAudioPlayer() {
        return optRegisteredAudioPlayer().orElseThrow(
                () -> new IllegalStateException("The voice connection with the identifier "
                        + clientConnectionInformation.getIdentifier() + " had no registered audio player!"
                )
        );
    }

    @Override
    public IVoiceConnectionInformation getVoiceConnectionInformation() {
        return clientConnectionInformation;
    }

    @Override
    public synchronized void setClientNickname(String nickname) {
        if (nickname == null || nickname.isEmpty() || nickname.length() > 30) {
            throw new IllegalArgumentException(
                    "The nickname of a client connection must be not empty and short than 30 characters!"
            );
        }

        clientConnectionInformation.setClientNickname(nickname);

        if (connected) {
            ts3jClientSocket.setNickname(nickname);
        }
    }

    @Override
    public void setShouldForwardTextMessages(boolean shouldForwardTextMessages) {
        this.shouldForwardTextMessages = shouldForwardTextMessages;
    }
}
