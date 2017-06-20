package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.T3Bot;
import de.fearnixx.t3.event.IEventManager;
import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.event.query.QueryEvent;
import de.fearnixx.t3.query.IQueryConnection;
import de.fearnixx.t3.query.IQueryRequest;
import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 22.05.17.
 */
public class QueryConnection extends Thread implements IQueryConnection {

    public static final int SOCKET_TIMEOUT_MILLIS = 500;
    public static final int KEEP_ALIVE_SECS = 240;
    public static final float REQ_DELAY = 0.5f;

    private ILogReceiver log;
    private IEventManager eventMgr;

    private int reqDelay;
    private boolean terminated;
    private Consumer<IQueryConnection> onClose;

    private SocketAddress addr;
    private final Socket mySock;
    private InputStream sIn;
    private OutputStream sOut;
    private OutputStreamWriter wOut;

    private final List<RequestContainer> reqQueue;
    private RequestContainer currentRequest;
    private QueryMessage currentResp;

    public QueryConnection(IEventManager eventMgr, ILogReceiver log, Consumer<IQueryConnection> onClose) {
        this.log = log;
        mySock = new Socket();
        reqQueue = new ArrayList<>();
        this.eventMgr = eventMgr;
        this.onClose = onClose;
    }

    public void setHost(String host, int port) {
        if (sIn != null) {
            throw new IllegalStateException("Cannot change connection information after connect");
        }
        addr = new InetSocketAddress(host, port);
    }

    public void open() throws IOException {
        if (sIn != null) {
            throw new IllegalStateException("Cannot reopen connections");
        }
        mySock.connect(addr, 8000);
        mySock.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
        sIn = mySock.getInputStream();
        sOut = mySock.getOutputStream();
        wOut = new OutputStreamWriter(sOut);
        byte[] buffer = new byte[1024];
        byte[] rbuff = new byte[1];
        int pos = 0;
        int lfC = 0;
        while (lfC < 2 && (sIn.read(rbuff, 0, 1)) != -1) {
            buffer[pos] = rbuff[0];
            if (rbuff[0] == '\n') {
                lfC = lfC + 1;
                if (lfC == 1) {
                    String greet = new String(buffer, pos - 3, 3, T3Bot.CHAR_ENCODING);
                    if (!"TS3".equals(greet)) {
                        log.severe("ATTENTION TS3Connection for " + addr + " received an invalid greeting: " + greet);
                    }
                }
            }
            pos++;
        }
        log.info("Connected to query at " + mySock.getInetAddress().toString());
    }

    @Override
    public void run() {

        final int[] timeout = new int[]{0};
        final boolean[] keepAliveSent = new boolean[]{false};
        IQueryRequest keepAliveReq = IQueryRequest.builder()
                .command("version")
                .build();

        int buffPos = 0;
        boolean lf = false;
        // TeamSpeak terminates command responses using LF
        final char lfChar = '\n';
        final char crChar = '\r';
        final byte[] buffer = new byte[128];
        final byte[] rByte = new byte[1];
        final StringBuilder largeBuff = new StringBuilder();

        boolean setNext;

        while (true) {
            try {
                synchronized (reqQueue) {
                    setNext = (currentRequest == null && reqQueue.size() > 0);
                }
                if (setNext) nextRequest();
                synchronized (mySock) {
                    if (terminated || sIn.read(rByte) == -1) {
                        terminated = true;
                        log.severe("Disconnected");
                        break;
                    }
                    timeout[0] = 0;
                    // Just cowardly discard this windoof character - We use *nix style LF
                    // Sidenote: TeamSpeak also accepts only \n
                    if (rByte[0] == crChar)
                        continue;
                    lf = rByte[0] == lfChar;
                    // Exclude LF for simplicity
                    if (!lf)
                        buffer[buffPos++] = rByte[0];
                    if (buffPos == buffer.length || lf) {
                        largeBuff.append(new String(Arrays.copyOf(buffer, buffPos), T3Bot.CHAR_ENCODING));
                        // Response complete? -> Send to processor
                        if (lf) {
                            processLine(largeBuff.toString());
                            largeBuff.delete(0, largeBuff.length());
                        }
                        buffPos = 0;
                    }
                }
            } catch (SocketTimeoutException e) {
                synchronized (mySock) {
                    if (reqDelay > 0)
                        reqDelay = reqDelay - SOCKET_TIMEOUT_MILLIS;
                }
                if ((++timeout[0] * SOCKET_TIMEOUT_MILLIS) >= (KEEP_ALIVE_SECS * 1000)) {
                    if (keepAliveSent[0]) {
                        log.severe("Connection lost - Read timed out");
                        kill();
                    } else {
                        log.finest("Sending keepalive");
                        keepAliveSent[0] = true;
                        sendRequest(keepAliveReq, r -> keepAliveSent[0] = false);
                    }
                }
            } catch (IOException e) {
                log.severe("Connection lost - Exception while reading", e);
                kill();
            }
        }
        if (onClose != null)
            onClose.accept(this);
    }

    private void processLine(String line) {
        // "notify" triggers events
        if (line.startsWith("notify")) {
            QueryMessage notification = new QueryMessage();
            try {
                notification.parseResponse(line);
            } catch (QueryParseException e) {
                log.severe("Failed to parse QueryMessage!", e);
                return;
            }
            IQueryEvent.INotification n = null;
            switch (notification.getType()) {
                case NOTIFYSERVER: n = new QueryEvent.Notification.Server(this, null, notification); break;
                case NOTIFYCHANNEL: n = new QueryEvent.Notification.Channel(this, null, notification); break;
                case NOTIFYTEXTSERVER: n = new QueryEvent.Notification.TextServer(this, null, notification); break;
                case NOTIFYTEXTCHANNEL: n = new QueryEvent.Notification.TextChannel(this, null, notification); break;
                case NOTIFYTEXTPRIVATE: n = new QueryEvent.Notification.TextPrivate(this, null, notification); break;
                default:
                    log.warning("Notification with type ", notification.getType(), " found! Ignoring.");
                    return;
            }
            eventMgr.fireEvent(n);
        } else {
            if (currentRequest == null) {
                log.warning("Received response without sending a request: ",
                        line.substring(0, line.length() >= 33 ? 33 : line.length()),
                        "...");
                return;
            }
            if (currentResp == null) {
                currentResp = new QueryMessage();
            }
            try {
                if (!currentResp.parseResponse(line)) {
                    log.warning("Response was unable to accept line: ",
                            line.substring(0, line.length() >= 33 ? 33 : line.length()),
                            "...");
                }
            } catch (QueryParseException e) {
                log.severe("Failed to parse QueryMessage!", e);
                return;
            }
            if (currentResp.isComplete()) {
                QueryEvent.Message event = new QueryEvent.Message(this, currentRequest.request, currentResp);
                try {
                    if (currentRequest.onDone != null)
                        currentRequest.onDone.accept(event);
                } catch (Exception e) {
                    log.severe("Encountered uncaught exception from callback!", e);
                }
                eventMgr.fireEvent(event);
                synchronized (reqQueue) {
                    currentRequest = null;
                    currentResp = null;
                }
            }
        }
    }

    private void nextRequest() {
        synchronized (mySock) {
            if (reqDelay > 0)
                return;
        }
        log.finer("Sending next request");
        IQueryRequest r = reqQueue.get(0).request;
        if (r.getCommand() == null || !r.getCommand().matches("^[a-z0-9_]+$")) {
            Throwable e = new IllegalArgumentException("Invalid request command used!").fillInStackTrace();
            log.warning("Encountered exception while preparing request", e);
            return;
        }

        StringBuilder reqB = new StringBuilder();
        if (r.getCommand().length() > 0)
            reqB.append(r.getCommand()).append(' ');

        String[] keys;
        int len;
        char[] key;
        char[] value;
        for (int i = 0; i < r.getChain().size(); i++) {
            keys = r.getChain().get(i).keySet().toArray(new String[r.getChain().get(i).keySet().size()]);
            for (int i1 = 0; i1 < keys.length; i1++) {
                len = keys[i1].length();
                key = new char[len];
                keys[i1].getChars(0, len, key, 0);
                len = r.getChain().get(i).get(keys[i1]).length();
                value = new char[len];
                r.getChain().get(i).get(keys[i1]).getChars(0, len, value, 0);
                reqB.append(new String(QueryEncoder.encodeBuffer(key))).append('=').append(new String(QueryEncoder.encodeBuffer(value)));
                if (i1 < keys.length-1)
                    reqB.append(' ');
            }
            if (i < r.getChain().size() - 1)
                reqB.append('|');
        }
        r.getOptions().forEach(o -> {
            if (reqB.length() > 0)
                reqB.append(' ');
            reqB.append(o);
        });

        synchronized (mySock) {
            if (!mySock.isConnected()) return;
            try {
                String msg = reqB.append('\n').toString();
                log.finest("-->", msg.substring(0, msg.length()-1));
                sOut.write(msg.getBytes());
                sOut.flush();
                reqDelay = Math.round(REQ_DELAY * SOCKET_TIMEOUT_MILLIS);
                synchronized (reqQueue) {
                    currentRequest = reqQueue.get(0);
                    reqQueue.remove(0);
                }
            } catch (IOException e) {
                log.warning("Failed to send request: ", e.getClass().getSimpleName(), e);
            }
        }
    }

    /* Interaction */

    public void sendRequest(IQueryRequest request, Consumer<IQueryEvent.IMessage> onDone) {
        RequestContainer c = new RequestContainer();
        c.request = request;
        c.onDone = onDone;
        synchronized (reqQueue) {
            reqQueue.add(c);
        }
    }

    public void sendRequest(IQueryRequest request) {
        sendRequest(request, null);
    }

    public boolean blockingLogin(Integer instID, String user, String pass) {
        IQueryRequest use = IQueryRequest.builder()
                .command("use")
                .addOption(instID.toString())
                .build();
        IQueryRequest login = IQueryRequest.builder()
                .command("login")
                .addOption(user)
                .addOption(pass)
                .build();
        // Wait for and lock receiver to prevent commands from returning too early
        final Map<Integer, IQueryEvent.IMessage> map = new ConcurrentHashMap<>(4);
        synchronized (mySock) {
            sendRequest(use, r -> map.put(0, r));
            sendRequest(login, r -> map.put(1, r));
        }
        try {
            while (map.getOrDefault(0, null) == null && map.getOrDefault(1, null) == null) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            log.severe("Login attempt interrupted - Failed");
            return false;
        }
        IQueryEvent.IMessage rUse = map.get(0);
        IQueryEvent.IMessage rLogin = map.get(0);
        boolean success = true;
        if (rUse.getMessage().getError().getID() != 0) {
            success = false;
            log.warning("Command 'use' failed: ", rUse.getMessage().getError().getID(), ' ',rUse.getMessage().getError().getMessage());
        }
        if (rLogin.getMessage().getError().getID() != 0) {
            success = false;
            log.warning("Command 'use' failed: ", rLogin.getMessage().getError().getID(), ' ', rLogin.getMessage().getError().getMessage());
        }
        return success;
    }

    /* RUNTIME CONTROL */

    /**
     * Kills the connection
     * This tries to properly shut down but doesn't care about success
     */
    public void kill() {
        synchronized (mySock) {
            if (terminated) return;
            log.warning("Kill requested");
            if (mySock.isConnected()) {
                try {
                    mySock.close();
                } catch (IOException e) {
                    log.warning(e);
                }
            }
            terminated = true;
        }
    }
}
