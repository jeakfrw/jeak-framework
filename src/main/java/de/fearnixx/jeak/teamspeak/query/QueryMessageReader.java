package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.teamspeak.except.QueryClosedException;
import de.fearnixx.jeak.teamspeak.query.parser.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.fearnixx.jeak.event.IRawQueryEvent.IMessage;

public class QueryMessageReader implements AutoCloseable {

    private static final Logger netLogger = LoggerFactory.getLogger("de.fearnixx.jeak.teamspeak.query.Netlog");

    private final InputStreamReader reader;
    private final QueryParser parser;

    private final StringBuilder largeBuffer = new StringBuilder();
    private final char[] buffer = new char[1024];
    private int bufferPos = 0;
    private char[] character = new char[1];
    private boolean closed = false;

    public QueryMessageReader(InputStream in,
                              Consumer<RawQueryEvent.Message.Notification> onNotification,
                              Consumer<RawQueryEvent.Message.Answer> onAnswer,
                              Consumer<Boolean> onGreetingStatus,
                              Supplier<IQueryRequest> requestSupplier) {

        this.reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        this.parser = new QueryParser(onNotification, onAnswer, onGreetingStatus, requestSupplier);
    }

    public void read() throws IOException {
        boolean gotLF = false;
        while (!isClosed() && reader.read(character) != -1) {

            if (character[0] == '\n') {
                gotLF = true;
                flushBuffer();

            } else if (character[0] == '\r') {
                // We ignore CR characters as UNIX-style LF is used.

            } else if (character[0] != '\0') {
                append();
            }

            if (gotLF) {
                // Reset buffer
                String input = largeBuffer.toString();
                largeBuffer.setLength(0);
                gotLF = false;

                netLogger.debug("<== {}", input);
                parser.parse(input);
            }
        }

        throw new QueryClosedException("End of input stream reached.");
    }

    private void append() {
        buffer[bufferPos++] = character[0];

        if (bufferPos == buffer.length) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        largeBuffer.append(buffer, 0, bufferPos);
        bufferPos = 0;
    }

    @Override
    public void close() throws IOException {
        if (!isClosed()) {
            closed = true;
            reader.close();
        }
    }

    public boolean isClosed() {
        return closed;
    }
}
