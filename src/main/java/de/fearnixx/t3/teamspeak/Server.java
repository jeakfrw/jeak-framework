package de.fearnixx.t3.teamspeak;

import de.fearnixx.t3.event.EventService;
import de.fearnixx.t3.reflect.IInjectionService;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.except.QueryConnectException;
import de.fearnixx.t3.teamspeak.query.QueryConnection;
import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;

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
    private int port;
    private String user;
    private String pass;
    private int instID;

    private final QueryConnection mainConnection;

    public Server(EventService eventService, ILogReceiver log) {
        this.log = log;
        this.eventService = eventService;
        mainConnection = new QueryConnection(this::onClose);
    }

    public void connect(String host, int port, String user, String pass, int instID) throws QueryConnectException {
        if (this.host!=null) {
            throw new RuntimeException("Can only connect a server once!");
        }
        injectService.injectInto(mainConnection);

        this.instID = instID;
        this.user = user;
        this.pass = pass;
        this.port = port;
        this.host = host;
        mainConnection.setHost(host, port);
        try {
            mainConnection.open();
        } catch (IOException e) {
            throw new QueryConnectException("Unable to open QueryConnection", e);
        }
        mainConnection.start();
        if (!mainConnection.blockingLogin(instID, user, pass)) {
            throw new QueryConnectException("BlockingLogin failed: See log");
        }
    }

    /* * * RUNTIME CONTROL * * */

    private void onClose(IQueryConnection conn) {
        if (conn == mainConnection) {
            shutdown();
        }
    }

    public void shutdown() {
        if (host != null) {
            mainConnection.kill();
        }
    }

    /* * * MISC * * */

    public IQueryConnection getConnection() {
        return mainConnection;
    }
}
