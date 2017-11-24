package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.ts3.chat.ChatMessage;
import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.keys.TargetType;

import java.nio.BufferOverflowException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public class QueryParser {

    /* Parsing */

    public static class Chars {

        public static final char PROPDIV = ' ';
        public static final char CHAINDIV = '|';
        public static final char PROPVALDIV = '=';
    }

    /**
     * Instance of the currently parsed message
     *  -> Messages are multi-lined ended by the "error"-response
     */
    private QueryMessage currentMessage;

    /**
     * Parse a query response
     * @param s The next line to parse
     * @return The message if finished - Notifications are one-liners thus don't interrupt receiving other messages
     */
    public Optional<QueryMessage> parse(String s) throws QueryParseException {
        try {
        /* Set up variables */
            QueryMessage workingMessage = null;
            List<QueryMessageObject> workingObjects;
            QueryMessageObject workingObject = null;

        /* Determine message type (check for notification) */
            int firstSpace = s.indexOf(Chars.PROPDIV);
            int firstEquals = s.indexOf(Chars.PROPVALDIV);
            String capt = null;
            if (firstSpace >= 0 && firstSpace < firstEquals) {
                // Response has a caption - extract it
                capt = s.substring(0, firstSpace).toLowerCase();
                s = s.substring(firstSpace + 1);
            }
            Class<?> notifType = null;
            boolean error = false;

            if (capt != null && capt.startsWith("notify")) {
                /* Message is a notification */
                String notifyCapt = capt.substring(6);

                if (notifyCapt.endsWith("view")) {
                    /* CLIENT ENTER/LEFT VIEW */
                    if (notifyCapt.startsWith("cliententer")) {
                        workingMessage = new QueryNotification.ClientEnterView();
                        notifType = IQueryNotification.IClientEnterView.class;
                    } else if (notifyCapt.startsWith("clientleft")) {
                        workingMessage = new QueryNotification.ClientLeaveView();
                        notifType = IQueryNotification.IClientLeaveView.class;
                    }

                } else if (notifyCapt.startsWith("textmessage")) {
                    /* TEXTMESSAGE */
                    workingMessage = new QueryNotification.TextMessage();
                    workingObject = new ChatMessage();
                    notifType = IQueryNotification.ITextMessage.class;

                } else if (notifyCapt.startsWith("client")) {
                    /* Other client event */
                    if (notifyCapt.endsWith("moved")) {
                        workingMessage = new QueryNotification.ClientMoved();
                        notifType = IQueryNotification.IClientMoved.class;
                    }
                }

                if (workingMessage == null) {
                    throw new QueryParseException("Failed to determine notification type: " + notifyCapt);
                }
            } else if (capt != null && capt.startsWith("error")) {
                error = true;
                if (currentMessage == null)
                    currentMessage = new QueryMessage();
                workingObject = new QueryMessageObject.Error();
                currentMessage.setError(((QueryMessageObject.Error) workingObject));
            } else {
                if (currentMessage == null)
                    currentMessage = new QueryMessage();
            }

        /* Assign working vars */
            if (workingMessage == null)
                workingMessage = currentMessage;
            if (workingObject == null)
                workingObject = new QueryMessageObject();
            workingObjects = workingMessage.getRawObjects();

            // TODO: Prevent array overflows e.g. for too large values
            if (!error)
                // Do not add the error object
                workingObjects.add(workingObject);
            boolean doKey = true; // Are we currently reading a property-key ?
            char[] keyBuff = new char[128]; // Buffer for the property key
            char[] valBuff = new char[2048]; // Buffer for the property value
            int keyBuffPos = 0; // Current position in the buffer
            int valBuffPos = 0; // Current position in the buffer
            int len = s.length();

            // Begin parsing the response
            for (int pos = 0; pos < len; pos++) {
                char c = s.charAt(pos);
                switch (c) {
                    case Chars.CHAINDIV:
                        // Rotate object
                        QueryMessageObject old = workingObject;
                        workingObject = new QueryMessageObject();
                        workingObject.copyFrom(old);
                        workingObjects.add(workingObject);
                        doKey = true;
                        keyBuffPos = 0;
                        valBuffPos = 0;
                    case '\n':
                    case Chars.PROPDIV:
                        // Flush current key and value
                        String key = new String(QueryEncoder.decodeBuffer(keyBuff, keyBuffPos));
                        String val = new String(QueryEncoder.decodeBuffer(valBuff, valBuffPos));
                        workingObject.setProperty(key, val);
                        keyBuffPos = 0;
                        valBuffPos = 0;
                        doKey = true;
                        break;
                    case Chars.PROPVALDIV:
                        if (doKey) {
                            valBuffPos = 0;
                            doKey = false;
                        }
                        break;
                    default:
                        if (doKey) {
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
            }
            if (notifType != null)
                workingMessage.setError(QueryMessageObject.Error.OK);

            if (workingMessage.getError() != null) {
                if (notifType == null)
                    currentMessage = null;
                else {
                    if (notifType == IQueryNotification.ITextMessage.class) {
                        int t = Integer.parseInt(workingObject.getProperty(PropertyKeys.TextMessage.TARGET_TYPE).get()) - 1;
                        ((QueryNotification.TextMessage) workingMessage).createTextMessage(TargetType.values()[t]);
                    }
                }
                return Optional.of(workingMessage);
            }
            return Optional.empty();
        } catch (Throwable t) {
            throw new QueryParseException("Generic error", t);
        }
    }
}
