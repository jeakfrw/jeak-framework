package de.fearnixx.t3.teamspeak;

import de.fearnixx.t3.event.EventService;
import de.fearnixx.t3.reflect.IInjectionService;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.teamspeak.query.*;
import de.fearnixx.t3.teamspeak.except.QueryConnectException;
import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class Server implements IServer {

    @Inject
    public ILogReceiver log;

    @Inject
    public IEventService eventService;

    @Inject
    public IInjectionService injectService;

    private volatile String host;

    private Thread connectionThread;
    private QueryConnectionAccessor mainConnection;

    public Server(EventService eventService, ILogReceiver log) {
        this.log = log;
        this.eventService = eventService;
        mainConnection = new QueryConnectionAccessor();
    }

    @SuppressWarnings("squid:S2095")
    public void connect(String host, int port, String user, String pass, int instID) {
        if (this.host != null) {
            throw new IllegalStateException("Can only connect a server once!");
        }

        try {
            this.host = host;
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(TS3Connection.SOCKET_TIMEOUT_MILLIS);

            injectService.injectInto(mainConnection);
            mainConnection.initialize(socket.getInputStream(), socket.getOutputStream());

            login(user, pass, instID);
            subscribeToEvents();

            connectionThread = new Thread(mainConnection);
            connectionThread.start();

        } catch (IOException e) {
            throw new QueryConnectException("Unable to open QueryConnection to " + host + ":" + port, e);
        }
    }

    private void login(String user, String pass, int instID) {
        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command("use")
                        .addOption(Integer.toString(instID))
                        .onError(answer -> {
                            log.severe("Failed to use desired instance: ", answer.getError().getMessage());
                            shutdown();
                        })
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder().command("login")
                        .addOption(user)
                        .addOption(pass)
                        .onError(answer -> {
                            log.severe("Failed to login to server: ", answer.getError().getMessage());
                            shutdown();
                        })
                        .build()
        );
    }

    private void subscribeToEvents() {
        final String servernotifyregisterCommand = "servernotifyregister";
        final String eventKey = "event";
        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(servernotifyregisterCommand)
                        .addKey(eventKey, "server")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(servernotifyregisterCommand)
                        .addKey(eventKey, "channel")
                        .addKey("id", "0")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(servernotifyregisterCommand)
                        .addKey(eventKey, "textserver")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(servernotifyregisterCommand)
                        .addKey(eventKey, "textchannel")
                        .build()
        );

        mainConnection.sendRequest(
                IQueryRequest.builder()
                        .command(servernotifyregisterCommand)
                        .addKey(eventKey, "textprivate")
                        .build()
        );
    }

    /* * * RUNTIME CONTROL * * */

    public void shutdown() {
        if (connectionThread != null) {
            mainConnection.shutdown();
            connectionThread.interrupt();
        }
    }

    /* * * MISC * * */

    public IQueryConnection getConnection() {
        return mainConnection;
    }
}
