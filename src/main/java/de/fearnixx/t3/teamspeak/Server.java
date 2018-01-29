package de.fearnixx.t3.teamspeak;

import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.service.task.ITask;
import de.fearnixx.t3.task.TaskService;
import de.fearnixx.t3.teamspeak.data.IChannel;
import de.fearnixx.t3.teamspeak.data.IClient;
import de.fearnixx.t3.teamspeak.data.TS3Channel;
import de.fearnixx.t3.teamspeak.data.TS3Client;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.QueryConnectException;
import de.fearnixx.t3.teamspeak.query.QueryConnection;
import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public class Server implements IServer {

    private ILogReceiver log;
    private IEventService eventService;

    private volatile String host;
    private int port;
    private String user;
    private String pass;
    private int instID;
    private final QueryConnection mainConnection;

    public Server(IEventService eventService, ILogReceiver log) {
        this.log = log;
        this.eventService = eventService;
        mainConnection = new QueryConnection(eventService, log.getChild("NET"), this::onClose);
    }

    public void connect(String host, int port, String user, String pass, int instID) throws QueryConnectException {
        if (this.host!=null) {
            throw new RuntimeException("Can only connect a server once!");
        }
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
        mainConnection.kill();
    }

    /* * * MISC * * */

    public IQueryConnection getConnection() {
        return mainConnection;
    }
}
