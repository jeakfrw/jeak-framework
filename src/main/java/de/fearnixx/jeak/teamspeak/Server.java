package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.IServiceManager;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.except.QueryConnectException;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.TSQueryConnectionDelegate;
import de.fearnixx.jeak.teamspeak.query.event.EventDispatcher;
import de.fearnixx.jeak.util.URIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
@FrameworkService(serviceInterface = IServer.class)
public class Server implements IServer {

    private static final String MSG_NOT_CONNECTED = "Not connected.";
    private static final boolean USE_VOICE_PORT_FOR_SELECTION = Main.getProperty("jeak.ts3.connectByVoicePort", false);
    private static final boolean OPEN_RAW_LISTENERS = Main.getProperty("jeak.ts3.openRawListeners", false);
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final AtomicInteger connectionCounter = new AtomicInteger();

    @Inject
    public IEventService eventService;

    @Inject
    public IInjectionService injectService;

    @Inject
    private IServiceManager serviceManager;

    @Inject
    public IBot bot;

    private final Object instanceLock = new Object();

    private EventDispatcher eventDispatcher = new EventDispatcher();
    private TeamSpeakConnectionFactory connector = new TeamSpeakConnectionFactory();
    private TSQueryConnectionDelegate mainConnection;
    private URIContainer connectionURI;

    /**
     * @deprecated #setCredentials has been replaced by {@link #setConnectionURI}
     */
    @Deprecated
    public void setCredentials(String host,
                               Integer queryPort,
                               String user, String pass, Integer instanceId,
                               boolean useSSL, String nickname) {
        Objects.requireNonNull(host, "Host may not be null!");
        Objects.requireNonNull(queryPort, "Port may not be null!");
        Objects.requireNonNull(user, "Username may not be null!");
        Objects.requireNonNull(pass, "Password may not be null!");
        Objects.requireNonNull(instanceId, "Instance id may not be null!");
        Objects.requireNonNull(nickname, "Nickname may not be null!");

        String portKey;
        if (USE_VOICE_PORT_FOR_SELECTION) {
            portKey = TeamSpeakConnectionFactory.QUERY_VOICEPORT;
        } else {
            portKey = TeamSpeakConnectionFactory.QUERY_INSTANCE;
        }
        final var scheme = useSSL ? TeamSpeakConnectionFactory.SCHEME_TLS_PLAINTEXT : TeamSpeakConnectionFactory.SCHEME_PLAINTEXT;
        final var queryArr = new String[][]{
                {TeamSpeakConnectionFactory.QUERY_USER, URLEncoder.encode(user)},
                {TeamSpeakConnectionFactory.QUERY_PASS, URLEncoder.encode(pass)},
                {portKey, Integer.toString(instanceId)},
                {TeamSpeakConnectionFactory.QUERY_NICKNAME, URLEncoder.encode(nickname)}
        };
        final var queryParams = Arrays.stream(queryArr)
                .map(a -> Arrays.stream(a)
                        .map(URLEncoder::encode)
                        .collect(Collectors.joining("=")))
                .collect(Collectors.joining("&"));
        try {
            setConnectionURI(new URI(String.format("%s://%s:%s?%s", scheme, host, queryPort, queryParams)));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The provided connection parameters are not valid!", e);
        }
    }

    public void setConnectionURI(URI connectionURI) {
        this.connectionURI = URIContainer.of(connectionURI);
    }

    @Override
    public void connect() {
        synchronized (instanceLock) {
            if (mainConnection != null && !mainConnection.isClosed()) {
                throw new IllegalStateException("Cannot call #connect on established connection!");
            }

            if (!connector.cdiDone()) {
                injectService.injectInto(connector);
                injectService.injectInto(eventDispatcher);
            }

            // Notify listeners.
            BotStateEvent.ConnectEvent.PreConnect preConnectEvent = new BotStateEvent.ConnectEvent.PreConnect();
            preConnectEvent.setBot(bot);
            eventService.fireEvent(preConnectEvent);

            // Establish connection.
            mainConnection = new TSQueryConnectionDelegate(connector.establishConnection(connectionURI));
            mainConnection.onAnswer(eventDispatcher::dispatchAnswer);
            mainConnection.onNotification(eventDispatcher::dispatchNotification);
            mainConnection.onClosed((conn, graceful) -> {
                BotStateEvent.ConnectEvent.Disconnect disconnectEvent =
                        new BotStateEvent.ConnectEvent.Disconnect(graceful);
                disconnectEvent.setBot(bot);
                eventService.fireEvent(disconnectEvent);
            });
            if (!OPEN_RAW_LISTENERS) {
                mainConnection.lockListeners("Connection managed by Jeak-framework! Please use the listeners and request callbacks instead.");
            } else {
                logger.warn("[CONSISTENCY] Enabling raw connection listeners bypasses framework mechanisms and should not be used.");
            }

            // Dispatch connection thread.
            Thread connectionThread = new Thread(mainConnection);
            connectionThread.setName("connection-" + connectionCounter.getAndIncrement());
            connectionThread.start();

            if (!connector.useInstance(mainConnection, mainConnection.getURI())) {
                logger.warn("==========================");
                logger.warn("Instance selection failed!");
                logger.warn("==========================");
                ensureClosed();
                throw new QueryConnectException("Instance selection failed!");
            }
            if (TeamSpeakConnectionFactory.requiresLoginCommands(mainConnection.getURI())
                    && !connector.attemptLogin(mainConnection, mainConnection.getURI())) {
                logger.warn("=============");
                logger.warn("Login failed!");
                logger.warn("=============");
                ensureClosed();
                throw new QueryConnectException("Login failed!");
            }


            logger.info("Connected!");
            subscribeToEvents();

            // Initialize WhoAmI-Information
            mainConnection.queueRequest(IQueryRequest.builder().command(QueryCommands.WHOAMI).build());

            // Notify listeners.
            BotStateEvent.ConnectEvent.PostConnect postConnectEvent = new BotStateEvent.ConnectEvent.PostConnect();
            postConnectEvent.setBot(bot);
            eventService.fireEvent(postConnectEvent);
            mainConnection.setNickName(
                    connectionURI.optSingleQuery(TeamSpeakConnectionFactory.QUERY_NICKNAME).orElse("Jeak")
            );
        }
    }

    protected void ensureClosed() {
        synchronized (instanceLock) {
            if (mainConnection != null && !mainConnection.isClosed()) {
                logger.debug("Closing active connection for reconnection.");
                try {
                    // To unify the lost connection event, we will close before the event is fired.
                    // This way, the order is the same as when the connection has been dropped from remote.
                    mainConnection.close();
                    logger.info("=======================================================");
                    logger.info("Connection aborted. See log above for more information.");
                    logger.info("=======================================================");
                } catch (Exception e) {
                    logger.warn("Failed to close active connection - will abandon.", e);
                }
            }
        }
    }

    private void subscribeToEvents() {
        final String eventKey = "event";
        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(QueryCommands.SERVER.SERVER_NOTIFY_REGISTER)
                        .addKey(eventKey, "server")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(QueryCommands.SERVER.SERVER_NOTIFY_REGISTER)
                        .addKey(eventKey, "channel")
                        .addKey("id", "0")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(QueryCommands.SERVER.SERVER_NOTIFY_REGISTER)
                        .addKey(eventKey, "textserver")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(QueryCommands.SERVER.SERVER_NOTIFY_REGISTER)
                        .addKey(eventKey, "textchannel")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(QueryCommands.SERVER.SERVER_NOTIFY_REGISTER)
                        .addKey(eventKey, "textprivate")
                        .build()
        );
    }

    /* * * Utility * * */

    @Override
    public IQueryRequest broadcastTextMessage(String message) {
        return IQueryRequest.builder()
                .command(QueryCommands.TEXTMESSAGE_SEND)
                .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.SERVER)
                .addKey(PropertyKeys.TextMessage.TARGET_ID, getInstanceId())
                .addKey(PropertyKeys.TextMessage.MESSAGE, message)
                .build();
    }

    @Override
    public IQueryRequest sendMessage(String message) {
        return broadcastTextMessage(message);
    }

    /* * * MISC * * */

    public IQueryConnection getConnection() {
        return optConnection()
                .orElseThrow(() -> new NoSuchElementException(MSG_NOT_CONNECTED));
    }

    @Override
    public Optional<IQueryConnection> optConnection() {
        if (isConnected()) {
            return Optional.of(mainConnection);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isConnected() {
        return mainConnection != null && !mainConnection.isClosed();
    }

    @Override
    public String getHost() {
        return Optional.ofNullable(connectionURI.getOriginalUri().getHost()).orElse("localhost");
    }

    @Override
    public int getPort() {
        final var uriPort = connectionURI.getOriginalUri().getPort();
        return uriPort > 0 ? uriPort : 10011;
    }

    @Override
    public int getVoicePort() {
        return optVoicePort()
                .orElseThrow(() -> new NoSuchElementException(MSG_NOT_CONNECTED));
    }

    @Override
    public Optional<Integer> optVoicePort() {
        return optServerInfoResponse()
                .flatMap(resp -> resp.getProperty(PropertyKeys.ServerInfo.PORT)
                        .map(Integer::parseInt));
    }

    @Override
    public Optional<IDataHolder> optServerInfoResponse() {
        final var request = IQueryRequest.builder().command(QueryCommands.SERVER.SERVER_INFO)
                .build();

        try {
            return Optional.of(getConnection().promiseRequest(request).get())
                    .map(a -> a.getDataChain().get(0));

        } catch (InterruptedException e) {
            logger.warn("Interrupted while retrieving server info.");
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException e) {
            logger.error("Unknown error retrieving server info.", e);
            return Optional.empty();
        }
    }

    @Override
    public int getInstanceId() {
        final var serverInfo = serviceManager.provideUnchecked(IDataCache.class).getInstanceInfo()
                .orElseThrow(() -> new IllegalStateException("Server information not known!"));
        return serverInfo.getProperty(PropertyKeys.ServerInfo.ID)
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalStateException("Server instance ID unknown?!"));
    }

    /**
     * @deprecated Since multiple connection modes are now supported, this should rather be taken from the connection URI.
     */
    @Deprecated
    public boolean isUseSSL() {
        return TeamSpeakConnectionFactory.SCHEME_TLS_PLAINTEXT.equals(connectionURI.getOriginalUri().getScheme());
    }

    @Override
    public String getNickname() {
        return optConnection()
                .orElseThrow(() -> new IllegalStateException("Not connected"))
                .getWhoAmI()
                .getProperty(PropertyKeys.Client.NICKNAME)
                .orElse("unknown");
    }

    @Listener
    public void onShutdown(IBotStateEvent.IPreShutdown event) {
        if (isConnected()) {
            try {
                mainConnection.shutdown();
            } catch (Exception e) {
                logger.error("Failed to close connection on shutdown!", e);
            }
        }
    }
}
