package de.fearnixx.jeak.teamspeak;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.IServiceManager;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.except.QueryConnectException;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.ITSQueryConnection;
import de.fearnixx.jeak.teamspeak.query.MessageMarshaller;
import de.fearnixx.jeak.teamspeak.query.TSQueryConnectionWrapper;
import de.fearnixx.jeak.teamspeak.query.channel.SerialMessageChannel;
import de.fearnixx.jeak.teamspeak.query.channel.StreamBasedChannel;
import de.fearnixx.jeak.util.URIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tlschannel.ClientTlsChannel;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("java:S2095")
public class TeamSpeakConnectionFactory {

    public static final int SOCKET_TIMEOUT_MILLIS = Main.getProperty("bot.connection.sotimeout", 100);
    public static final String TEAMSPEAK_SSH_CHANNEL_TYPE = "shell";

    public static final String LOCALHOST_ADDR = "localhost";
    public static final String SCHEME_PLAINTEXT = "telnet";
    public static final String SCHEME_TLS_PLAINTEXT = "tls";
    public static final String SCHEME_SSH = "ssh";
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";

    public static final String QUERY_INSTANCE = "instanceid";
    public static final String QUERY_VOICEPORT = "voiceport";
    public static final String QUERY_USER = "user";
    public static final String QUERY_USERNAME_DEFAULT = "serveradmin";
    public static final String QUERY_PASS = "password";
    public static final String QUERY_API_KEY = "apikey";
    public static final String QUERY_NICKNAME = "nickname";

    private static final Logger logger = LoggerFactory.getLogger(TeamSpeakConnectionFactory.class);

    private static final JSch jSecureChannel = new JSch();
    private static final String SSH_CONF_HOST_CHECKS = "StrictHostKeyChecking";

    public static boolean requiresLoginCommands(URIContainer connURI) {
        return SCHEME_PLAINTEXT.equals(connURI.getOriginalUri().getScheme())
                || SCHEME_TLS_PLAINTEXT.equals(connURI.getOriginalUri().getScheme());
    }

    @Inject
    private IServiceManager serviceManager;

    protected MessageMarshaller createMarshaller() {
        return new MessageMarshaller(serviceManager.provideUnchecked(IUserService.class));
    }

    protected TSQueryConnectionWrapper createConnection(ByteChannel messageChannel) {
        return new TSQueryConnectionWrapper(new SerialMessageChannel(messageChannel), createMarshaller());
    }

    protected TSQueryConnectionWrapper connectWithSocket(ByteChannel socketChannel) throws IOException {
        return createConnection(socketChannel);
    }

    protected SocketChannel createPlaintext(URIContainer connectionUri) throws IOException {
        logger.warn("[DEPRECATION] Plaintext query connections are deprecated by TeamSpeak! Please consider migrating.");
        final int port = connectionUri.getOriginalUri().getPort() > 0 ?
                connectionUri.getOriginalUri().getPort()
                : 443;
        final String host = Optional.ofNullable(connectionUri.getOriginalUri().getHost()).orElse(LOCALHOST_ADDR);
        return SocketChannel.open(new InetSocketAddress(host, port));
    }

    protected ByteChannel createTLS(URIContainer connectionUri) throws IOException {
        final int port = connectionUri.getOriginalUri().getPort() > 0 ?
                connectionUri.getOriginalUri().getPort()
                : 443;
        final String host = Optional.ofNullable(connectionUri.getOriginalUri().getHost()).orElse(LOCALHOST_ADDR);
        final SocketChannel rawChannel = SocketChannel.open(new InetSocketAddress(host, port));
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new QueryConnectException("SSLContext initialization failed!", e);
        }
        return ClientTlsChannel.newBuilder(rawChannel, sslContext).build();
    }

    protected TSQueryConnectionWrapper connectWithSSH(URIContainer connectionURI) {
        try {
            final var session = getSSHSession(connectionURI);
            final var channel = session.openChannel(TEAMSPEAK_SSH_CHANNEL_TYPE);
            final var input = channel.getInputStream();
            final var output = channel.getOutputStream();
            ((ChannelShell) channel).setPty(false);
            channel.connect(SOCKET_TIMEOUT_MILLIS);
            return createConnection(StreamBasedChannel.create(input, output));
        } catch (JSchException e) {
            throw new QueryConnectException("Failed to establish SSH connection!", e);
        } catch (IOException e) {
            throw new QueryConnectException("Failed to open SSH channel streams!", e);
        }
    }

    protected Session getSSHSession(URIContainer connectionUri) throws JSchException {
        final int port = connectionUri.getOriginalUri().getPort() > 0 ?
                connectionUri.getOriginalUri().getPort()
                : 443;
        final String host = Optional.ofNullable(connectionUri.getOriginalUri().getHost())
                .orElse(LOCALHOST_ADDR);
        final String user = connectionUri.optSingleQuery(QUERY_USER)
                .orElse(QUERY_USERNAME_DEFAULT);
        final String password = connectionUri.optSingleQuery(QUERY_PASS)
                .orElseThrow(() -> new IllegalArgumentException("Connection URI does not contain a password!"));

        final var session = jSecureChannel.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig(SSH_CONF_HOST_CHECKS, "no");
        session.connect(SOCKET_TIMEOUT_MILLIS);
        return session;
    }

    /**
     * @return the created and connected {@link ITSQueryConnection}.
     * @throws QueryConnectException when the connection failed for any reason.
     * @apiNote When this method fails, the caller can issue another connection attempt by re-calling this method. The connector itself is stateless with regard to the created connections.
     */
    public ITSQueryConnection establishConnection(URIContainer connUri) {
        Objects.requireNonNull(connUri, "Connection URI may not be null!");

        if (!connUri.hasQuery(QUERY_API_KEY) && !connUri.hasQuery(QUERY_PASS)) {
            throw new IllegalArgumentException("Connection URI does not contain sufficient credentials!");
        }

        logger.info("Trying to connect to TeamSpeak on {}", connUri);
        TSQueryConnectionWrapper conn;
        try {
            switch (connUri.getOriginalUri().getScheme()) {
                case SCHEME_PLAINTEXT:
                    conn = connectWithSocket(createPlaintext(connUri));
                    break;
                case SCHEME_TLS_PLAINTEXT:
                    conn = connectWithSocket(createTLS(connUri));
                    break;
                case SCHEME_SSH:
                    conn = connectWithSSH(connUri);
                    break;
                case SCHEME_HTTP:
                case SCHEME_HTTPS:
                    throw new IllegalStateException("HTTP(S) scheme is not yet supported!");
                default:
                    throw new QueryConnectException("Unknown connection scheme: " + connUri);
            }
        } catch (IOException e) {
            throw new QueryConnectException(String.format("Failed to connect to server using %s", connUri), e);
        }

        conn.setURI(connUri);
        return conn;
    }

    public boolean useInstance(TSQueryConnectionWrapper conn) {
        final var connURI = conn.getURI();
        final var commandBuilder =
                IQueryRequest.builder().command(QueryCommands.SERVER.USE_INSTANCE);
        if (connURI.hasQuery(QUERY_VOICEPORT) && !connURI.hasQuery(QUERY_INSTANCE)) {
            logger.debug("Using voice port for 'use' command.");
            commandBuilder.addKey("port", connURI.optSingleQuery(QUERY_VOICEPORT)
                    .orElseThrow(() -> new IllegalArgumentException("No value for voice port given in URI.")));
        } else {
            logger.debug("Using instance id for 'use' command.");
            commandBuilder.addOption(connURI.optSingleQuery(QUERY_INSTANCE).orElse("1"));
        }

        try {
            final var answer = conn.promiseRequest(commandBuilder.build()).get();
            return answer.getErrorCode() == 0;

        } catch (InterruptedException e) {
            logger.warn("Interrupted while selecting instance!", e);
            Thread.currentThread().interrupt();
            return false;

        } catch (ExecutionException e) {
            logger.error("Failed to select instance!", e);
            return false;
        }
    }

    public boolean attemptLogin(TSQueryConnectionWrapper conn) {
        final var connURI = conn.getURI();
        final var request = IQueryRequest.builder().command(QueryCommands.SERVER.LOGIN)
                .addOption(connURI.optSingleQuery(TeamSpeakConnectionFactory.QUERY_USER).orElse(TeamSpeakConnectionFactory.QUERY_USERNAME_DEFAULT))
                .addOption(connURI.optSingleQuery(TeamSpeakConnectionFactory.QUERY_PASS).orElseThrow())
                .build();
        try {
            final var answer = conn.promiseRequest(request).get();
            return answer.getErrorCode() == 0;

        } catch (InterruptedException e) {
            logger.warn("Interrupted while logging in.");
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            logger.error("Unknown error logging in.", e);
            return false;
        }
    }

    public boolean cdiDone() {
        return serviceManager != null;
    }
}
