package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.except.QueryConnectException;
import de.fearnixx.jeak.teamspeak.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
@FrameworkService(serviceInterface = IServer.class)
public class Server implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final AtomicInteger connectionCounter = new AtomicInteger();
    private static final boolean USE_VOICE_PORT_FOR_SELECTION = Main.getProperty("jeak.ts3.connectByVoicePort", false);

    @Inject
    public IEventService eventService;

    @Inject
    public IInjectionService injectService;

    @Inject
    public IBot bot;

    private final Object LOCK = new Object();

    private String host;
    private int queryPort;
    private String user;
    private String pass;
    private int instanceId;
    private boolean useSSL;
    private String nickname;

    private QueryConnectionAccessor mainConnection;
    private IDataHolder serverInfoResponse = null;

    public void setCredentials(String host,
                               Integer queryPort,
                               String user, String pass, Integer instanceId,
                               Boolean useSSL, String nickname) {
        synchronized (LOCK) {
            Objects.requireNonNull(host, "Host may not be null!");
            Objects.requireNonNull(queryPort, "Port may not be null!");
            Objects.requireNonNull(user, "Username may not be null!");
            Objects.requireNonNull(pass, "Password may not be null!");
            Objects.requireNonNull(instanceId, "Instance id may not be null!");
            Objects.requireNonNull(useSSL, "SSL flag may not be null!");
            Objects.requireNonNull(nickname, "Nickname may not be null!");

            this.host = host;
            this.queryPort = queryPort;
            this.user = user;
            this.pass = pass;
            this.instanceId = instanceId;
            this.useSSL = useSSL;
            this.nickname = nickname;
        }
    }

    @Override
    public void connect() {
        synchronized (LOCK) {
            if (host == null) {
                throw new IllegalStateException("Cannot connect without credentials");
            }

            ensureClosed(true);

            try {
                logger.info("Trying to connect to {}:{}", host, queryPort);
                // Notify listeners.
                BotStateEvent.ConnectEvent.PreConnect preConnectEvent = new BotStateEvent.ConnectEvent.PreConnect();
                preConnectEvent.setBot(bot);
                eventService.fireEvent(preConnectEvent);

                tcpConnectAndInitialize();

                // Dispatch connection thread.
                Thread connectionThread = new Thread(mainConnection);
                connectionThread.setName("connection-" + connectionCounter.getAndIncrement());
                connectionThread.start();
            } catch (IOException e) {
                throw new QueryConnectException("Unable to open QueryConnection to " + host + ":" + queryPort, e);
            }
        }
    }

    @SuppressWarnings("squid:S2095")
    protected void tcpConnectAndInitialize() throws IOException {
        Socket socket;
        if (!useSSL) {
            socket = new Socket(host, queryPort);
        } else {
            logger.info("SSL: Enabled.");
            socket = SSLSocketFactory.getDefault().createSocket(host, queryPort);
            ((SSLSocket) socket).startHandshake();
        }
        socket.setSoTimeout(TS3Connection.SOCKET_TIMEOUT_MILLIS);

        mainConnection = new QueryConnectionAccessor();
        injectService.injectInto(mainConnection);
        mainConnection.initialize(socket.getInputStream(), socket.getOutputStream());

        login(user, pass, instanceId, result -> {
            if (!result) {
                BotStateEvent failedEvent = new BotStateEvent.ConnectEvent.ConnectFailed();
                failedEvent.setBot(bot);
                eventService.fireEvent(failedEvent);
            } else {
                logger.info("Connected successfully.");
                subscribeToEvents();
                mainConnection.setNickName(nickname);
                // Notify listeners.
                BotStateEvent.ConnectEvent.PostConnect postConnectEvent = new BotStateEvent.ConnectEvent.PostConnect();
                postConnectEvent.setBot(bot);
                eventService.fireEvent(postConnectEvent);
            }
        });
    }

    protected void ensureClosed(boolean emitDisconnect) {
        if (mainConnection != null && !mainConnection.isClosed()) {
            logger.debug("Closing active connection for reconnection.");
            try {
                // To unify the lost connection event, we will close before the event is fired.
                // This way, the order is the same as when the connection has been dropped from remote.
                mainConnection.close();

                if (emitDisconnect) {
                    BotStateEvent.ConnectEvent.Disconnect event = new BotStateEvent.ConnectEvent.Disconnect(false);
                    event.setBot(bot);
                    eventService.fireEvent(event);
                }
            } catch (Exception e) {
                logger.warn("Failed to close active connection - will abandon.", e);
            }
        }
    }

    private void login(String user, String pass, int instID, Consumer<Boolean> resultCallback) {
        AtomicBoolean useResult = new AtomicBoolean(true);
        AtomicBoolean loginResult = new AtomicBoolean(true);
        QueryBuilder useCommandBuilder = IQueryRequest.builder()
                .command(QueryCommands.SERVER.USE_INSTANCE);
        if (USE_VOICE_PORT_FOR_SELECTION) {
            useCommandBuilder.addKey("port", instID);
        } else {
            useCommandBuilder.addOption(Integer.toString(instID));
        }

        mainConnection.sendRequest(
                useCommandBuilder
                        .onError(answer -> {
                            logger.error("Failed to use desired instance: {}", answer.getError().getMessage());
                            ensureClosed(false);
                            useResult.set(false);
                            resultCallback.accept(false);
                        })
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder().command(QueryCommands.SERVER.LOGIN)
                        .addOption(user)
                        .addOption(pass)
                        .onError(answer -> {
                            if (useResult.get()) {
                                logger.error("Failed to login to server: {}", answer.getError().getMessage());
                                ensureClosed(false);
                                resultCallback.accept(false);
                            }
                            loginResult.set(false);
                        })
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder().command(QueryCommands.SERVER.SERVER_INFO)
                        .onError(answer -> {
                            if (useResult.get() && loginResult.get()) {
                                logger.error("Failed to login to retrieve server information? Am I allowed to do that (b_virtualserver_info_view) ? TS3: {} - {}",
                                        answer.getErrorMessage(), answer.getErrorCode());
                                ensureClosed(false);
                                resultCallback.accept(false);
                            }
                        })
                        .onSuccess(answer -> {
                            serverInfoResponse = answer.getDataChain().get(0);
                            resultCallback.accept(true);
                        })
                        .build()
        );
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
    public IQueryRequest sendMessage(String message) {
        return IQueryRequest.builder()
                .command(QueryCommands.TEXTMESSAGE_SEND)
                .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.SERVER)
                .addKey(PropertyKeys.TextMessage.TARGET_ID, instanceId)
                .addKey(PropertyKeys.TextMessage.MESSAGE, message)
                .build();
    }

    /* * * MISC * * */

    public IQueryConnection getConnection() {
        return optConnection()
                .orElseThrow(() -> new NoSuchElementException("Not connected."));
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
        return host;
    }

    @Override
    public int getPort() {
        return queryPort;
    }

    @Override
    public int getVoicePort() {
        return optVoicePort()
                .orElseThrow(() -> new NoSuchElementException("Not connected."));
    }

    @Override
    public Optional<Integer> optVoicePort() {
        return optConnection()
                .flatMap(conn -> serverInfoResponse.getProperty(PropertyKeys.ServerInfo.PORT)
                        .map(Integer::parseInt));
    }

    @Override
    public Optional<IDataHolder> optServerInfoResponse() {
        return Optional.ofNullable(serverInfoResponse);
    }

    @Override
    public int getInstanceId() {
        return instanceId;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Listener
    public void onShutdown(IBotStateEvent.IPreShutdown event) {
        if (isConnected()) {
            mainConnection.shutdown();
        }
    }
}
