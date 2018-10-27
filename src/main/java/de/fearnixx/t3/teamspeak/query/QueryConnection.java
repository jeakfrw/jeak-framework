package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.Main;
import de.fearnixx.t3.T3Bot;
import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;

import de.fearnixx.t3.reflect.IInjectionService;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.except.QueryException;
import de.fearnixx.t3.teamspeak.except.QueryParseException;
import de.fearnixx.t3.teamspeak.query.parser.QueryParser;
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
@Deprecated
public class QueryConnection extends Thread implements IQueryConnection {

    public static final int SOCKET_TIMEOUT_MILLIS = Main.getProperty("bot.connection.sotimeout", 500);
    public static final int KEEP_ALIVE_SECS = Main.getProperty("bot.connection.keepalive", 240);
    public static final float REQ_DELAY = Main.getProperty("bot.connection.reqdelay", 0.25f);

    @Inject
    public ILogReceiver log;

    @Inject
    public IInjectionService injectionService;

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

    private final List<IQueryRequest> reqQueue;
    private IQueryRequest currentRequest;
    private QueryParser parser;

    private Integer instanceID;
    private IDataHolder whoamiResponse;

    public QueryConnection(Consumer<IQueryConnection> onClose) {
        this.mySock = new Socket();
        this.reqQueue = new ArrayList<>();
        this.parser = new QueryParser();
        this.onClose = onClose;
        this.notifier = new QueryNotifier();
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
        injectionService.injectInto(notifier, "CONN");

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
        Integer hashCode = line.hashCode();
        try {
            optMessage = parser.parse(line);
        } catch (QueryParseException e) {
            log.severe("Failed to parse QueryMessage!", e);
            return;
        }
        if (!optMessage.isPresent())
            return;

        RawQueryEvent.Message event = optMessage.get();
        event.setConnection(this);

        try {
            notifier.processEvent(event, hashCode);
            synchronized (reqQueue) {
                parser.setCurrentRequest(null);
                currentRequest = null;
            }
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
        IQueryRequest request = reqQueue.get(0);
        if (request.getCommand() == null || !request.getCommand().matches("^[a-z0-9_]+$")) {
            Throwable e = new IllegalArgumentException("Invalid request command used!").fillInStackTrace();
            log.warning("Encountered exception while preparing request", e);
            return;
        }

        final String message = buildSocketMessage(request);

        synchronized (mySock) {
            if (!mySock.isConnected()) return;
            try {
                if (netDumpOutput != null) {
                    try {
                        String ln = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + " ==> " + message;
                        if (!ln.endsWith("\n"))
                            ln = ln + "\n";
                        netDumpOutput.write(ln);
                    } catch (IOException e) {
                        log.warning("Failed to write to network dump - disabling", e);
                        netDumpOutput = null;
                    }
                }
                log.finest(ANSIColors.Font.CYAN, ANSIColors.Background.BLACK, " --> ", ANSIColors.RESET, message);
                sOut.write(message.getBytes());
                sOut.write('\n');

                reqDelay = Math.round(REQ_DELAY * SOCKET_TIMEOUT_MILLIS);
                synchronized (reqQueue) {
                    currentRequest = reqQueue.remove(0);
                }
                parser.setCurrentRequest(currentRequest);
                sOut.flush();
            } catch (IOException e) {
                log.warning("Failed to send request: ", e.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Builds the message for the TS3 query from a request.
     */
    private String buildSocketMessage(IQueryRequest request) {
        StringBuilder sockMessage = new StringBuilder();

        // Append: Command
        if (request.getCommand().length() > 0) {
            sockMessage.append(request.getCommand()).append(' ');
        }

        // Append: Objects
        // (Chain of `key=val key2=val2...` separated by '|')
        List<IDataHolder> dataChain = request.getDataChain();
        final int chainLength = dataChain.size();
        final int chainLastIndex = chainLength - 1;

        for (int i = 0; i < chainLength; i++) {
            // Copy the mapping in order to avoid concurrent modification
            Map<String, String> properties = new HashMap<>(dataChain.get(i).getValues());
            String[] keys = properties.keySet().toArray(new String[0]);

            for (int j = 0; j < keys.length; j++) {
                String propKey = keys[j];
                int keyLen = propKey.length();
                char[] propKeyParts = new char[keyLen];
                propKey.getChars(0, keyLen, propKeyParts, 0);

                String propValue = properties.get(keys[j]);
                int valLen = propValue.length();
                char[] propValParts = new char[valLen];
                propValue.getChars(0, valLen, propValParts, 0);

                char[] encodedKeyParts = QueryEncoder.encodeBuffer(propKeyParts);
                char[] encodedValParts = QueryEncoder.encodeBuffer(propValParts);
                sockMessage.append(encodedKeyParts)
                        .append('=')
                        .append(encodedValParts);

                if (j < keys.length-1) {
                    sockMessage.append(' ');
                }
            }

            if (i < chainLastIndex) {
                sockMessage.append('|');
            }
        }

        // Append: Options
        request.getOptions().forEach(o -> {
            if (sockMessage.length() > 0)
                sockMessage.append(' ');
            sockMessage.append(o);
        });

        return sockMessage.toString();
    }

    /* DEBUG */

    public void setNetworkDump(File file) {
        try {
            if (file.isDirectory())
                throw new IOException("Blocked by directory! ");

            if ((!file.isFile() || !file.exists())
                    && !file.createNewFile())
                throw new IOException("Failed to create file.");

            netDumpOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

        } catch (IOException e) {
            log.warning("Unable to open dump path for writing: ", file.toString(), e);
            netDumpOutput = null;
        }
    }

    /* Interaction */

    /**
     * @deprecated by {@link IQueryConnection#sendRequest(IQueryRequest, Consumer)} deprecation.
     */
    @Deprecated
    @Override
    public void sendRequest(IQueryRequest request, Consumer<IRawQueryEvent.IMessage.IAnswer> onDone) {
        QueryBuilder replacement = QueryBuilder.from(request);

        if (request.onDone() != null) {
            replacement.onDone(request.onDone()
                    .andThen(answer -> onDone.accept(((IRawQueryEvent.IMessage.IAnswer) answer.getRawReference()))));
        }

        sendRequest(request);
    }

    @Override
    public void sendRequest(IQueryRequest request) {
        synchronized (reqQueue) {
            reqQueue.add(request);
        }
    }

    @Override
    public PromisedRequest promiseRequest(IQueryRequest request) {
        final PromisedRequest promise = new PromisedRequest(request);
        sendRequest(request, promise::setAnswer);
        return promise;
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
                whoamiResponse.setProperty(PropertyKeys.Client.NICKNAME, newNick);
        });
    }

    public void loadWhoAmI() {
        sendRequest(IQueryRequest.builder().command("whoami").build(), msg -> {
            whoamiResponse = msg;
        });
    }

    @Override
    public IDataHolder getWhoAmI() {
        return whoamiResponse;
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
