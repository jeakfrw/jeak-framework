package de.fearnixx.jeak.teamspeak.voice.connection;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.voice.connection.info.AbstractClientConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.connection.info.ConfigClientConnectionInformation;
import de.fearnixx.jeak.teamspeak.voice.connection.info.DbClientConnectionInformation;
import de.fearnixx.jeak.voice.connection.IClientConnection;
import de.fearnixx.jeak.voice.connection.IClientConnectionService;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.config.FileConfig;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@FrameworkService(serviceInterface = IClientConnectionService.class)
public class ClientConnectionService implements IClientConnectionService {

    @Inject
    private IServer server;

    @Inject
    private IBot bot;

    private boolean isDatabaseConnected = false;

    private Map<String, ClientConnection> clientConnections = new HashMap<>();

    @Override
    public boolean isConnectionAvailable(String identifier) {
        final ClientConnection connection = clientConnections.get(identifier);
        return connection == null || !connection.isLocked();
    }

    @Override
    public IClientConnection getClientConnection(String identifier) {
        if (clientConnections.containsKey(identifier)) {
            final ClientConnection clientConnection = clientConnections.get(identifier);

            if (clientConnection.isLocked()) {
                throw new IllegalStateException("Tried to access a locked client connection!");
            }

            return clientConnection;
        }


        final LocalIdentity teamspeakIdentity = createTeamspeakIdentity();

        AbstractClientConnectionInformation newClientConnectionInformation;

        if (isDatabaseConnected) {
            //TODO: Store client connection information in database
            newClientConnectionInformation = new DbClientConnectionInformation();
        } else {
            newClientConnectionInformation = new ConfigClientConnectionInformation(
                    new FileConfig(LoaderFactory.getLoader("application/json"),
                            new File(bot.getConfigDirectory(), "frw/voice/" + identifier + ".json")),
                    identifier
            );
            newClientConnectionInformation.setClientNickname(identifier);
            newClientConnectionInformation.setLocalIdentity(teamspeakIdentity);
        }

        final ClientConnection clientConnection = new ClientConnection(
                newClientConnectionInformation,
                server.getHost(),
                server.getPort()
        );

        clientConnections.put(identifier, clientConnection);

        return clientConnection;
    }

    @Listener
    public void preShutdown(IBotStateEvent.IPreShutdown event) {
        clientConnections.values().stream().filter(ClientConnection::isConnected).forEach(
                c -> {
                    try {
                        c.disconnect();
                    } catch (TimeoutException e) {
                        //Probably the server is unreachable and the connection is already disconnected
                    }
                }
        );
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
            localIdentity = LocalIdentity.generateNew(15);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to create local identity");
        }
        return localIdentity;
    }
}
