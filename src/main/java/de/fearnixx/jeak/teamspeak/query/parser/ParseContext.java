package de.fearnixx.jeak.teamspeak.query.parser;

import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.teamspeak.except.QueryParseException;
import de.fearnixx.jeak.teamspeak.query.QueryEncoder;

import java.nio.BufferOverflowException;

public class ParseContext<T extends RawQueryEvent.Message> {

    private static final int KEY_BUFFER_SIZE = 1024;
    private static final int VAL_BUFFER_SIZE = 4096;

    private final T first;
    private RawQueryEvent.Message last;
    private RawQueryEvent.Message working;
    private RawQueryEvent.ErrorMessage error;

    private final char[] keyBuffer = new char[KEY_BUFFER_SIZE];
    private int keyBuffPos = 0;

    private final char[] valBuffer = new char[VAL_BUFFER_SIZE];
    private int valBuffPos = 0;

    public ParseContext(T first) {
        this.first = first;
        this.working = first;
        this.last = first;
    }

    public void addToKeyBuffer(char character) {
        if (keyBuffPos >= keyBuffer.length) {
            throw new QueryParseException("Key buffer exceeded!", new BufferOverflowException());
        }
        keyBuffer[keyBuffPos++] = character;
    }

    private String getDecodedKey() {
        return new String(QueryEncoder.decodeBuffer(keyBuffer, keyBuffPos));
    }

    public void addToValBuffer(char character) {
        if (valBuffPos >= valBuffer.length) {
            throw new QueryParseException("Value buffer exceeded!", new BufferOverflowException());
        }
        valBuffer[valBuffPos++] = character;
    }

    private String getDecodedValue() {
        return new String(QueryEncoder.decodeBuffer(valBuffer, valBuffPos));
    }

    public void flushProperty() {
        working.setProperty(
                getDecodedKey(),
                getDecodedValue()
        );
        keyBuffPos = 0;
        valBuffPos = 0;
    }

    public void nextObject(RawQueryEvent.Message next) {
        last.setNext(next);
        next.setPrevious(last);
        next.copyFrom(working);

        last = last.getNext();
        working = next;
    }

    public void closeContext() {
        if (keyBuffPos > 0) {
            flushProperty();
        }
    }

    public void setError(RawQueryEvent.Message.ErrorMessage error) {
        RawQueryEvent.Message msg = first;
        while (msg != null) {
            msg.setError(error);
            msg = msg.getNext();
        }
        this.error = error;
        working = error;
    }

    public boolean isClosed() {
        return error != null;
    }

    public T getMessage() {
        return first;
    }
}
