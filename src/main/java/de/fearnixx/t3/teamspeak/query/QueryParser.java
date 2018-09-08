package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent.Message;
import de.fearnixx.t3.teamspeak.except.QueryParseException;

import javax.annotation.Nullable;
import java.nio.BufferOverflowException;
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
     * Instance of the request that's currently running
     * -> Flushed when Message ended
     */
    private RequestContainer currentRequest;

    /**
     * Instance of the currently parsed answer
     *  -> Messages are multi-lined ended by the "error"-response
     */
    private RawQueryEvent.Message currentFirst;

    /**
     * Parse a query response
     * @param s The next line to parse
     * @return The message if finished - Notifications are one-liners thus don't interrupt receiving other messages
     */
    public Optional<RawQueryEvent.Message> parse(String s) {
        try {
            // Determine message type (check for notification)
            int firstSpace = s.indexOf(Chars.PROPDIV);
            int firstEquals = s.indexOf(Chars.PROPVALDIV);
            String capt = null;
            if (firstSpace >= 0 && firstSpace < firstEquals) {
                // Response has a caption - extract it
                capt = s.substring(0, firstSpace).toLowerCase();
                s = s.substring(firstSpace + 1);
            }
            boolean error = "error".equals(capt);
            boolean notify = capt != null && capt.startsWith("notify");

            // Setup variables accordingly
            Message workingFirst;
            Message workingMessage;
            if (error) {
                // This message is an error message
                Message.ErrorMessage errorMessage = new Message.ErrorMessage(currentRequest.getRequest());
                if (currentFirst != null) {
                    workingFirst = currentFirst;
                    workingMessage = currentFirst;
                } else {
                    workingFirst = errorMessage;
                    workingMessage = errorMessage;
                }

                do {
                    workingMessage.setError(errorMessage);
                } while ((workingMessage = workingMessage.getNext()) != null);
                workingMessage = errorMessage;

            } else if (notify){
                // This message is a notification
                Message.Notification message = new Message.Notification();
                capt = capt.substring(6);
                message.setCaption(capt);
                workingFirst = message;
                workingMessage = message;
            } else {
                if (currentFirst == null) {
                    currentFirst = new Message.Answer(currentRequest.getRequest());
                }
                workingFirst = currentFirst;
                workingMessage = currentFirst;
            }

            // Reference to last message in the current chain
            Message workingLast = workingFirst;
            while (workingLast.hasNext())
                workingLast = workingLast.getNext();

            // TODO: Prevent array overflows e.g. for too large values
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
                    case '\n':
                    case Chars.CHAINDIV:
                        // Flush current key and value
                        if (!doKey) {
                            String key = new String(QueryEncoder.decodeBuffer(keyBuff, keyBuffPos));
                            String val = new String(QueryEncoder.decodeBuffer(valBuff, valBuffPos));
                            workingMessage.setProperty(key, val);
                            doKey = true;
                            keyBuffPos = 0;
                            valBuffPos = 0;
                        }

                        if (!error) {
                            // Connect current message to chain
                            workingLast.setNext(workingMessage);
                            workingMessage.setPrevious(workingLast);

                            // Move to the new end of the chain
                            workingLast = workingMessage;

                            // Create the new current message
                            if (notify) {
                                workingMessage = new Message.Notification();
                                ((Message.Notification) workingMessage).setCaption(capt);
                            } else {
                                workingMessage = new Message.Answer(currentRequest.getRequest());
                            }
                            workingMessage.copyFrom(workingLast);
                        }
                        break;
                    case Chars.PROPDIV:
                        // Flush current key and value
                        String key = new String(QueryEncoder.decodeBuffer(keyBuff, keyBuffPos));
                        String val = new String(QueryEncoder.decodeBuffer(valBuff, valBuffPos));
                        workingMessage.setProperty(key, val);
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
            // Did we reach a chain end?
            if (error || workingFirst.getError() != null) {
                if (workingFirst instanceof Message.Answer) {
                    // Flush cache
                    currentFirst = null;
                }
                return Optional.of(workingFirst);
            }
            return Optional.empty();
        } catch (Exception t) {
            throw new QueryParseException("An exception was encountered during parsing.", t);
        }
    }

    public void setCurrentRequest(RequestContainer container) {
        this.currentRequest = container;
    }
}
