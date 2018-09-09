package de.fearnixx.t3.teamspeak.query.parser;

import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.teamspeak.except.QueryParseException;
import de.fearnixx.t3.teamspeak.query.QueryEncoder;

import java.nio.BufferOverflowException;

public class ParseContext {

    private static final int KEY_BUFFER_SIZE = 1024;
    private static final int VAL_BUFFER_SIZE = 4096;

    private RawQueryEvent.Message first;
    private RawQueryEvent.Message last;
    private RawQueryEvent.Message working;
    private RawQueryEvent.ErrorMessage error;

    private char[] keyBuffer = new char[KEY_BUFFER_SIZE];
    private int keyBuffPos = 0;

    private char[] valBuffer = new char[VAL_BUFFER_SIZE];
    private int valBuffPos = 0;

    public ParseContext(RawQueryEvent.Message first) {
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

    public void addToVBalBuffer(char character) {
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
        last.setNext(working);
        working.setPrevious(last);
        next.copyFrom(working);

        last = working;
        working = next;
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

    public RawQueryEvent.Message getMessage() {
        return first;
    }
}
