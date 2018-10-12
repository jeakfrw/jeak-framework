package de.fearnixx.t3.teamspeak.query.parser;

import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent.Message;
import de.fearnixx.t3.teamspeak.except.QueryParseException;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;

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
    private IQueryRequest currentRequest;

    /**
     * Instance of the currently parsed answer
     * -> Messages are multi-lined ended by the "error"-response
     */
    private ParseContext context;

    /**
     * Parse a query response
     *
     * @param input The next line to parse
     * @return The message if finished - Notifications are one-liners thus don't interrupt receiving other messages
     */
    public Optional<Message> parse(String input) {
        try {
            ParseInfo parseInfo = new ParseInfo();
            input = parseInfo.inspect(input);

            ParseContext currentContext = getParseContextFor(parseInfo);
            parseToContext(input, parseInfo, currentContext);

            if (parseInfo.isNotification) {
                currentContext.setError(RawQueryEvent.ErrorMessage.OK());
            }

            if (currentContext.isClosed()) {
                if (!parseInfo.isNotification) {
                    context = null;
                }
                return Optional.of(currentContext.getMessage());
            } else {
                return Optional.empty();
            }
        } catch (Exception t) {
            throw new QueryParseException("An exception was encountered during parsing.", t);
        }
    }

    /**
     * Returns the parsing context in regard to the peek information.
     * Determines whether or not to slot in a Notification context or to continue parsing on the current answer context.
     */
    private ParseContext getParseContextFor(ParseInfo parseInfo) {
        ParseContext currentContext;

        if (parseInfo.isNotification) {
            // This message is a notification
            Message.Notification message = new Message.Notification();
            message.setCaption(parseInfo.caption);
            currentContext = new ParseContext(message);

        } else {
            if (context == null) {
                context = new ParseContext(new Message.Answer(currentRequest));
            }

            if (parseInfo.isError) {
                // This message is an isError message
                Message.ErrorMessage errorMessage = new Message.ErrorMessage(currentRequest);
                context.setError(errorMessage);
            }

            currentContext = context;
        }
        return currentContext;
    }

    /**
     * Actually parses the input to objects of the parse context.
     */
    private void parseToContext(String input, ParseInfo parseInfo, ParseContext parseContext) {
        boolean doKey = true; // Are we currently reading a property-key ?
        int len = input.length();

        // Begin parsing the response
        for (int pos = 0; pos < len; pos++) {
            char c = input.charAt(pos);

            switch (c) {
                case '\n':
                case Chars.CHAINDIV:
                    // Flush current key and value
                    parseContext.flushProperty();
                    doKey = true;

                    if (!parseInfo.isError) {
                        Message next;
                        if (parseInfo.isNotification) {
                            next = new Message.Notification();
                            ((Message.Notification) next).setCaption(parseInfo.caption);
                        } else {
                            next = new Message.Answer(currentRequest);
                        }
                        parseContext.nextObject(next);
                    }
                    break;

                case Chars.PROPDIV:
                    parseContext.flushProperty();
                    doKey = true;
                    break;

                case Chars.PROPVALDIV:
                    if (doKey)
                        doKey = false;
                    else
                        parseContext.addToValBuffer(c);
                    break;

                default:
                    if (doKey)
                        parseContext.addToKeyBuffer(c);
                    else
                        parseContext.addToValBuffer(c);
            }
        }
    }

    public void setCurrentRequest(IQueryRequest request) {
        this.currentRequest = request;
    }

    /**
     * Extracts basic information about the input by peeking into it.
     */
    public static class ParseInfo {
        private String caption;

        private boolean isError;
        private boolean isNotification;

        public String inspect(String input) {
            int firstSpace = input.indexOf(Chars.PROPDIV);
            int firstEquals = input.indexOf(Chars.PROPVALDIV);

            // Determine message type (check for notification)
            caption = null;
            if (firstSpace >= 0 && firstSpace < firstEquals) {
                // Response has a caption - extract it
                caption = input.substring(0, firstSpace).toLowerCase();
                input = input.substring(firstSpace + 1);
            }
            isError = "error".equals(caption);
            isNotification = caption != null && caption.startsWith("notify");

            if (isNotification) {
                caption = caption.substring(6);
            }

            return input;
        }
    }
}
