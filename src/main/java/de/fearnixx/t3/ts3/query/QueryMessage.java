package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.query.IQueryMessage;
import de.fearnixx.t3.query.IQueryMessageObject;
import de.fearnixx.t3.query.IQueryNotification;

import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryMessage implements IQueryMessage {

    private IQueryMessage.MsgType type = MsgType.RESPONSE;
    private List<QueryMessageObject> objects = new ArrayList<>();
    private QueryMessageObject.Error error;
    private QueryNotification notification;

    /* Parsing */

    public static class Chars {
        public static final char PROPDIV = ' ';
        public static final char CHAINDIV = '|';
        public static final char PROPVALDIV = '=';
    }

    /**
     * Parse a query response
     * @param s
     * @return If the response has been parsed
     */
    public boolean parseResponse(String s) throws QueryParseException {
        if (isComplete()) {
            return false;
        }
        int firstSpace = s.indexOf(Chars.PROPDIV);
        int firstEquals = s.indexOf(Chars.PROPVALDIV);
        String capt = null;
        if (firstSpace >= 0 && firstSpace < firstEquals) {
            // Response has a caption - extract it
            capt = s.substring(0, firstSpace).toLowerCase();
            s = s.substring(firstSpace + 1);
        }

        // TODO: Prevent array overflows e.g. for too large values
        QueryMessageObject workingObj = new QueryMessageObject();
        addObject(workingObj);
        boolean doKey = true; // Are we currently reading a property-key ?
        char[] keyBuff = new char[128]; // Buffer for the property key
        char[] valBuff = new char[2048]; // Buffer for the property value
        int keyBuffPos = 0; // Current position in the buffer
        int valBuffPos = 0; // Current position in the buffer

        // Begin parsing the response
        for (int pos = 0; pos < s.length(); pos++) {
            char c = s.charAt(pos);
            if (c == Chars.CHAINDIV) {
                // Rotate object
                workingObj = new QueryMessageObject();
                addObject(workingObj);
                doKey = true;
                keyBuffPos = 0;
                valBuffPos = 0;
            } else if (c == Chars.PROPDIV) {
                // Flush current key and value
                String key = new String(QueryEncoder.decodeBuffer(keyBuff, keyBuffPos));
                String val = new String(QueryEncoder.decodeBuffer(valBuff, valBuffPos));
                workingObj.setProperty(key, val);
                keyBuffPos = 0;
                valBuffPos = 0;
                doKey = true;
            } else if (doKey && c == Chars.PROPVALDIV) {
                valBuffPos = 0;
                doKey = false;
            } else if (doKey) {
                if (keyBuffPos >= keyBuff.length) {
                    throw new QueryParseException("Key buffer exceeded!", new BufferOverflowException());
                }
                keyBuff[keyBuffPos++] = c;
            } else if (!doKey) {
                if (valBuffPos >= valBuff.length) {
                    throw new QueryParseException("Value buffer exceeded!", new BufferOverflowException());
                }
                valBuff[valBuffPos++] = c;
            }
        }
        if (capt != null  && "error".equals(capt)) {
            error = workingObj.new Error();
        } else if (capt != null && capt.startsWith("notify")) {
            String notifyCapt = capt.substring(6);
            IQueryNotification.NotifyType type = IQueryNotification.NotifyType.UNKNOWN;
            if (notifyCapt.endsWith("view")) {
                if (notifyCapt.startsWith("cliententer")) {
                    type = IQueryNotification.NotifyType.VIEW_CLIENT_ENTER;
                } else if (notifyCapt.startsWith("clientleft")) {
                    type = IQueryNotification.NotifyType.VIEW_CLIENT_LEAVE;
                }
            }
            if (type == IQueryNotification.NotifyType.UNKNOWN) {
                throw new QueryParseException("Failed to determine notification type: " + notifyCapt);
            }
            this.notification = new QueryNotification(type);
            error = QueryMessageObject.ERROR_OK;
        }
        return true;
    }

    /* /Parsing */

    @Override
    public MsgType getType() {
        return type;
    }

    public void addObject(QueryMessageObject obj) {
        objects.add(obj);
    }

    public boolean isComplete() {
        return error != null;
    }

    @Override
    public int getObjectCount() {
        return objects.size() > 0 ? objects.size()-1 : 0;
    }

    @Override
    public IQueryMessageObject getObject(int index) {
        if (index < 0 || index > objects.size())
            throw new IndexOutOfBoundsException("No object at: " + index);
        return objects.get(index);
    }

    @Override
    public List<IQueryMessageObject> getObjects() {
        return Collections.unmodifiableList(objects.subList(0, objects.size()-1));
    }

    @Override
    public Optional<IQueryNotification> getNotification() {
        return Optional.ofNullable(notification);
    }

    @Override
    public QueryMessageObject.Error getError() {
        return error;
    }
}
