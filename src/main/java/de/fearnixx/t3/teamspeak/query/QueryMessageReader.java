package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.teamspeak.except.QueryClosedException;
import de.fearnixx.t3.teamspeak.query.parser.QueryParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import static de.fearnixx.t3.event.query.RawQueryEvent.Message.Message;

public class QueryMessageReader {

    private final InputStreamReader reader;
    private final QueryParser parser;

    private final StringBuilder largeBuffer = new StringBuilder();
    private final char[] buffer = new char[1024];
    private int bufferPos = 0;
    private char[] character = new char[1];

    public QueryMessageReader(InputStream in, Consumer<Message> onNotification, Consumer<Message> onAnswer) {
        this.reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        this.parser = new QueryParser(onNotification, onAnswer);
    }

    public void readMessage() throws IOException {
        boolean gotLF = false;
        while (reader.read(character) != -1) {

            if (character[0] == '\n') {
                gotLF = true;
                flushBuffer();

            } else if (character[0] == '\r') {
                // We ignore CR characters as UNIX-style LF is used.

            } else {
                append();
            }

            if (gotLF) {
                parser.parse(largeBuffer.toString());
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
}
