package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.Main;
import de.fearnixx.t3.T3Bot;
import de.fearnixx.t3.event.EventService;
import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;

import de.fearnixx.t3.service.event.IEventService;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.DataHolder;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.except.QueryException;
import de.fearnixx.t3.teamspeak.query.except.QueryParseException;
import de.mlessmann.logging.ANSIColors;
import de.mlessmann.logging.ILogReceiver;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by MarkL4YG on 22.05.17.
 */
public class QueryConnection extends Thread implements IQueryConnection {

    public static final int SOCKET_TIMEOUT_MILLIS = Main.getProperty("bot.connection.sotimeout", 500);
    public static final int KEEP_ALIVE_SECS = Main.getProperty("bot.connection.keepalive", 240);
    public static final float REQ_DELAY = Main.getProperty("bot.connection.reqdelay", 0.25f);

    private ILogReceiver log;
    private IEventService eventService;
    private QueryNotifier notifier;

    private int reqDelay;
    private boolean terminated;
    private Consumer<IQueryConnection> onClose;

    private SocketAddress addr;
    private final Socket mySock;
    private InputStream sIn;
    private OutputStream sOut;
    private OutputStreamWriter wOut;
    private BufferedWriter netDumpOutput;

    private final List<RequestContainer> reqQueue;
    private PromisedRequest currentRequestPromise;
    private RequestContainer currentRequest;
    private QueryParser parser;

    private Integer instanceID;
    private IDataHolder whoami;
    private int lastMessageHash = 0;

    public QueryConnection(EventService eventService, ILogReceiver log, Consumer<IQueryConnection> onClose) {
        this.log = log;
        this.mySock = new Socket();
        this.reqQueue = new ArrayList<>();
        this.parser = new QueryParser();
        this.eventService = eventService;
        this.onClose = onClose;
        this.notifier = new QueryNotifier(eventService);
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
        boolean lf;
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
        if (netDumpOutput != null) {
            try {
                String ln = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                            + " <== "
                            + line;
                if (!ln.endsWith("\n"))
                    ln = ln + "\n";
                netDumpOutput.write(ln);
                netDumpOutput.flush();
            } catch (IOException e) {
                log.warning("Failed to write to network dump - disabling", e);
                netDumpOutput = null;
            }
        }
        boolean err = line.startsWith("error");
        boolean errOc = !line.startsWith("error id=0");
        String col;
        String blockCol = null;
        String arrow = " <-- ";
        if (!err)
            col = ANSIColors.Font.CYAN + ANSIColors.Background.BLACK;
        else if (errOc) {
            col = ANSIColors.Font.RED + ANSIColors.Background.BLACK;
            blockCol = ANSIColors.Background.RED;
        } else {
            col = ANSIColors.Font.CYAN + ANSIColors.Background.BLACK;
        }
        int len = line.length();
        if (len > 120) {
            log.finest(col, arrow, ANSIColors.RESET, blockCol, line.substring(0, 120), "...", ANSIColors.RESET);
        } else {
            log.finest(col, arrow, ANSIColors.RESET, blockCol, line.substring(0, len-1), ANSIColors.RESET, ' ');
        }
        Optional<RawQueryEvent.Message> optMessage;
        try {
            optMessage = parser.parse(line);
        } catch (QueryParseException e) {
            log.severe("Failed to parse QueryMessage!", e);
            return;
        }
        if (!optMessage.isPresent())
            return;

        RawQueryEvent.Message event = optMessage.get();
        if (event instanceof RawQueryEvent.Message.Answer) {
            if (currentRequest != null && currentRequest.onDone != null) {
                currentRequest.onDone.accept(((RawQueryEvent.Message.Answer) event));
            }
            synchronized (reqQueue) {
                parser.setCurrentRequest(null);
                currentRequest = null;
            }
        }
        try {
            notifier.processEvent(event);
        } catch (QueryException e) {
            log.severe("Got an exception while processing a message!", e);
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
                if (netDumpOutput != null) {
                    try {
                        String ln = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + " ==> " + reqB.toString();
                        if (!ln.endsWith("\n"))
                            ln = ln + "\n";
                        netDumpOutput.write(ln);
                    } catch (IOException e) {
                        log.warning("Failed to write to network dump - disabling", e);
                        netDumpOutput = null;
                    }
                }
                String msg = reqB.append('\n').toString();
                log.finest(ANSIColors.Font.CYAN, ANSIColors.Background.BLACK, " --> ", ANSIColors.RESET, msg.substring(0, msg.length()-1));
                sOut.write(msg.getBytes());
                sOut.flush();
                reqDelay = Math.round(REQ_DELAY * SOCKET_TIMEOUT_MILLIS);
                synchronized (reqQueue) {
                    currentRequest = reqQueue.get(0);
                    parser.setCurrentRequest(currentRequest);
                    reqQueue.remove(0);
                }
            } catch (IOException e) {
                log.warning("Failed to send request: ", e.getClass().getSimpleName(), e);
            }
        }
    }

    /* DEBUG */

    public void setNetworkDump(File file) {
        if (file.isDirectory()) {
            log.warning("Network dump path is a directory: ", file.toString());
            return;
        }
        try {
            if (!file.isFile() || !file.exists())
                file.createNewFile();
            netDumpOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        } catch (IOException e) {
            log.warning("Unable to open dump path for writing: ", file.toString(), e);
            netDumpOutput = null;
        }
    }

    /* Interaction */

    @Override
    public void sendRequest(IQueryRequest request, Consumer<IRawQueryEvent.IMessage.IAnswer> onDone) {
        RequestContainer c = new RequestContainer();
        c.request = request;
        c.onDone = onDone;
        synchronized (reqQueue) {
            reqQueue.add(c);
        }
    }

    @Override
    public void sendRequest(IQueryRequest request) {
        sendRequest(request, null);
    }

    @Override
    public PromisedRequest promiseRequest(IQueryRequest request) {
        final PromisedRequest promise = new PromisedRequest(request);
        sendRequest(request, promise::setAnswer);
        return promise;
    }

    @Override
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
        IQueryRequest whoami = IQueryRequest.builder()
                                            .command("whoami")
                                            .build();
        // Wait for and lock receiver to prevent commands from returning too early
        final Map<Integer, IRawQueryEvent.IMessage.IAnswer> map = new ConcurrentHashMap<>(4);
        synchronized (mySock) {
            sendRequest(use, r -> map.put(0, r));
            sendRequest(login, r -> map.put(1, r));
            sendRequest(whoami, r -> map.put(2, r));
        }
        try {
            while (map.getOrDefault(0, null) == null
                   || map.getOrDefault(1, null) == null
                   || map.getOrDefault(2, null) == null) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            log.severe("Login attempt interrupted - Failed");
            return false;
        }
        IRawQueryEvent.IMessage.IAnswer rUse = map.get(0);
        IRawQueryEvent.IMessage.IAnswer rLogin = map.get(1);
        IRawQueryEvent.IMessage.IAnswer rWhoAmI = map.get(2);
        this.whoami = rWhoAmI;
        boolean success = true;
        if (rUse.getError().getCode() != 0) {
            success = false;
            log.warning("Command 'use' failed: ", rUse.getError().toString());
            instanceID = instID;
        }
        if (rLogin.getError().getCode() != 0) {
            success = false;
            log.warning("Command 'login' failed: ", rLogin.getError().toString());
        }
        if (rWhoAmI.getError().getCode() != 0) {
            success = false;
            log.warning("Command 'whoami' failed: ", rWhoAmI.getError().toString());
        }
        return success;
    }

    @Override
    public Integer getInstanceID() {
        return instanceID;
    }

    @Override
    public void setNickName(String newNick) {
        if (newNick == null) return;
        IQueryRequest r = IQueryRequest.builder()
                                       .command("clientupdate")
                                       .addKey(PropertyKeys.Client.NICKNAME, newNick)
                                       .build();
        sendRequest(r, msg -> {
            if (msg.getError().getCode() == 0)
                whoami.setProperty(PropertyKeys.Client.NICKNAME, newNick);
        });
    }

    public void loadWhoAmI() {
        sendRequest(IQueryRequest.builder().command("whoami").build(), msg -> {
            whoami = msg;
        });
    }

    @Override
    public IDataHolder getWhoAmI() {
        return whoami;
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
            if (netDumpOutput != null) {
                try {
                    netDumpOutput.write("=== CLOSED ===");
                    netDumpOutput.close();
                } catch (IOException e) {
                    // pass
                }
                netDumpOutput = null;
            }
            terminated = true;
        }
    }
}
