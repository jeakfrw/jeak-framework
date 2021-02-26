package de.fearnixx.jeak.teamspeak.voice.connection;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.NotificationReason;
import de.fearnixx.jeak.teamspeak.voice.connection.info.AbstractVoiceConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.connection.info.ConfigVoiceConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.connection.info.DbVoiceConnectionInformation;
import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionService;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionStore;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.config.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

@FrameworkService(serviceInterface = IVoiceConnectionService.class)
public class VoiceConnectionService implements IVoiceConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(VoiceConnectionService.class);

    private static final int CONNECTION_FACTORY_POOL_SIZE = Main.getProperty("jeak.voice_connection.construction_poolSize", 2);

    @Inject
    private IServer server;

    @Inject
    private IBot bot;

    @Inject
    private IUserService userService;

    @Inject
    private IEventService eventService;

    private final ExecutorService requestExecutorService = Executors.newFixedThreadPool(CONNECTION_FACTORY_POOL_SIZE);

    private boolean isDatabaseConnected = false;

    private final Map<String, VoiceConnection> clientConnections = new ConcurrentHashMap<>();

    @Override
    public void requestVoiceConnection(String identifier, Consumer<Optional<IVoiceConnection>> onRequestFinished) {
        requestExecutorService.execute(
                () -> {
                    synchronized (clientConnections) {
                        logger.info("Request for voice connection {}", identifier);

                        if (clientConnections.containsKey(identifier)) {
                            logger.debug("Voice connection already requested.");
                            final VoiceConnection clientConnection = clientConnections.get(identifier);

                            if (clientConnection.isConnected()) {
                                onRequestFinished.accept(Optional.empty());
                                return;
                            }

                            onRequestFinished.accept(Optional.of(clientConnection));
                            return;
                        }

                        AbstractVoiceConnectionInformation newClientConnectionInformation;

                        if (isDatabaseConnected) {
                            newClientConnectionInformation = new DbVoiceConnectionInformation();
                        } else {
                            newClientConnectionInformation = new ConfigVoiceConnectionInformation(
                                    new FileConfig(LoaderFactory.getLoader("application/json"),
                                            new File(bot.getConfigDirectory(), "frw/voice/" + identifier + ".json")),
                                    identifier
                            );

                            if (newClientConnectionInformation.getTeamspeakIdentity() == null) {
                                logger.debug("Creating new TS-identity.");
                                final LocalIdentity teamspeakIdentity = createTeamspeakIdentity();
                                newClientConnectionInformation.setLocalIdentity(teamspeakIdentity);
                            }
                        }

                        final IntSupplier portSupplier = () -> server.optVoicePort()
                                .orElseThrow(() -> new IllegalStateException("Couldn't get voice port! Query not connected yet?"));
                        final VoiceConnection clientConnection = new VoiceConnection(
                                newClientConnectionInformation,
                                server.getHost(),
                                portSupplier,
                                eventService,
                                bot,
                                userService
                        );

                        clientConnections.put(identifier, clientConnection);
                        logger.info("Successfully constructed voice connection {}. Running callback.", identifier);

                        onRequestFinished.accept(Optional.of(clientConnection));
                    }
                }
        );
    }

    @Listener
    public void onLeaveEvent(IQueryEvent.INotification.IClientLeave event) {
        NotificationReason reason = event.getReason();

        if (reason == NotificationReason.SERVER_KICK || reason == NotificationReason.BANNED) {
            Optional<String> optKickedIdentifier = clientConnections.entrySet().stream()
                    .filter(e -> e.getValue().getClientId() == event.getTarget().getClientID())
                    .map(Map.Entry::getKey)
                    .findFirst();

            if (optKickedIdentifier.isPresent()) {
                String kickedIdentifier = optKickedIdentifier.get();
                logger.warn(
                        "Voice connection {} has been kicked or banned from the server. Removing from connection cache.",
                        kickedIdentifier
                );

                clientConnections.remove(kickedIdentifier);
            }
        }
    }

    @Listener
    public void postShutdown(IBotStateEvent.IPostShutdown event) {
        logger.info("Running shutdown.");
        clientConnections.values().forEach(VoiceConnection::shutdown);
        clientConnections.clear();
    }

    @Listener
    public void onPreInit(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        File voiceDir = new File(bot.getConfigDirectory(), "frw/voice");

        if (!voiceDir.isDirectory() && !voiceDir.mkdirs()) {
            throw new IllegalStateException("Failed to create voice connection directory!");
        }
    }

    private LocalIdentity createTeamspeakIdentity() {
        LocalIdentity localIdentity;
        try {
            final Integer securityLevel = Main.getProperty("jeak.voice_connection.security_level", 15);
            localIdentity = LocalIdentity.generateNew(securityLevel);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create local identity!", e);
        }
        return localIdentity;
    }

    @Override
    public IVoiceConnectionStore createVoiceConnectionStore() {
        return new VoiceConnectionStore(this);
    }

    @Override
    public IVoiceConnectionStore createVoiceConnectionStore(String... identifiers) {
        for (int i = 0; i < identifiers.length - 1; i++) {
            String identifier = identifiers[i];

            for (int j = i + 1; j < identifiers.length; j++) {
                if (identifiers[j].equals(identifier)) {
                    throw new IllegalArgumentException(
                            "The given identifiers were not unique! Duplicate: " + identifier
                    );
                }
            }
        }

        final VoiceConnectionStore voiceConnectionStore = new VoiceConnectionStore(this);
        Arrays.stream(identifiers).forEach(voiceConnectionStore::prepareVoiceConnection);

        return voiceConnectionStore;
    }
}
