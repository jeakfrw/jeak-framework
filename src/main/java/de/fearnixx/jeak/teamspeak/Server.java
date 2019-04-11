package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.except.QueryConnectException;
import de.fearnixx.jeak.teamspeak.query.IQueryConnection;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryConnectionAccessor;
import de.fearnixx.jeak.teamspeak.query.TS3Connection;
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
public class Server implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final AtomicInteger connectionCounter = new AtomicInteger();

    @Inject
    public IEventService eventService;

    @Inject
    public IInjectionService injectService;

    @Inject
    public IBot bot;

    private final Object LOCK = new Object();

    private String host;
    private int port;
    private String user;
    private String pass;
    private int instanceId;
    private boolean useSSL;
    private String nickname;

    private Thread connectionThread;
    private QueryConnectionAccessor mainConnection;

    public void setCredentials(String host, Integer port, String user, String pass, Integer instanceId, Boolean useSSL, String nickname) {
        synchronized (LOCK) {
            Objects.requireNonNull(host, "Host may not be null!");
            Objects.requireNonNull(port, "Port may not be null!");
            Objects.requireNonNull(user, "Username may not be null!");
            Objects.requireNonNull(pass, "Password may not be null!");
            Objects.requireNonNull(instanceId, "Instance id may not be null!");
            Objects.requireNonNull(useSSL, "SSL flag may not be null!");
            Objects.requireNonNull(nickname, "Nickname may not be null!");

            this.host = host;
            this.port = port;
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
                logger.info("Trying to connect to {}:{}", host, port);
                // Notify listeners.
                BotStateEvent.ConnectEvent.PreConnect preConnectEvent = new BotStateEvent.ConnectEvent.PreConnect();
                preConnectEvent.setBot(bot);
                eventService.fireEvent(preConnectEvent);

                tcpConnectAndInitialize();

                // Dispatch connection thread.
                connectionThread = new Thread(mainConnection);
                connectionThread.setName("connection-" + connectionCounter.getAndIncrement());
                connectionThread.start();
            } catch (IOException e) {
                throw new QueryConnectException("Unable to open QueryConnection to " + host + ":" + port, e);
            }
        }
    }

    @SuppressWarnings("squid:S2095")
    protected void tcpConnectAndInitialize() throws IOException {
        Socket socket;
        if (!useSSL) {
            socket = new Socket(host, port);
        } else {
            logger.info("SSL: Enabled.");
            socket = SSLSocketFactory.getDefault().createSocket(host, port);
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
        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(QueryCommands.SERVER.USE_INSTANCE)
                        .addOption(Integer.toString(instID))
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
                        })
                        .onSuccess(answer -> resultCallback.accept(true))
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

    @Listener
    public void onShutdown(IBotStateEvent.IPreShutdown event) {
        if (isConnected()) {
            mainConnection.shutdown();
        }
    }
}
