package de.fearnixx.jeak.teamspeak.query.parser;

import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.event.query.RawQueryEvent.Message;
import de.fearnixx.jeak.teamspeak.except.QueryParseException;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public class QueryParser {

    private static final Logger logger = LoggerFactory.getLogger(QueryParser.class);

    /* Parsing */
    public static class Symbols {

        private Symbols() {
            // Hide public constructor.
        }

        public static final char PROPDIV = ' ';
        public static final char CHAINDIV = '|';
        public static final char PROPVALDIV = '=';

        public static final String[] GREETINGS = {
                "TS3",
                "Welcome to the TeamSpeak 3 ServerQuery"
        };
    }

    private final Supplier<IQueryRequest> requestSupplier;

    /**
     * Instance of the currently parsed answer
     * -> Messages are multi-lined ended by the "error"-response
     */
    private ParseContext<Message.Answer> context;
    private int greetingPos = 0;
    private final Consumer<Boolean> onGreetingStatus;


    private final Consumer<Message.Notification> onNotification;
    private final Consumer<Message.Answer> onAnswer;

    public QueryParser(Consumer<Message.Notification> onNotification,
                       Consumer<Message.Answer> onAnswer,
                       Consumer<Boolean> onGreetingStatus,
                       Supplier<IQueryRequest> requestSupplier) {
        this.onNotification = onNotification;
        this.onAnswer = onAnswer;
        this.onGreetingStatus = onGreetingStatus;
        this.requestSupplier = requestSupplier;
    }

    /**
     * Parse a query response
     *
     * @param input The next line to parse
     * @return The message if finished - Notifications are one-liners thus don't interrupt receiving other messages
     */
    public Optional<Message> parse(String input) {

        if (greetingPos < Symbols.GREETINGS.length) {
            if (input.startsWith(Symbols.GREETINGS[greetingPos])) {
                greetingPos++;
                logger.debug("Received greeting part: {}", greetingPos);

                onGreetingStatus.accept(greetingPos >= Symbols.GREETINGS.length);
                return Optional.empty();
            } else {
                throw new IllegalStateException("Invalid DATA received while awaiting greeting.");
            }
        }

        try {
            ParseInfo parseInfo = new ParseInfo();
            input = parseInfo.inspect(input);

            if (parseInfo.isNotification) {
                Message.Notification notification = new Message.Notification();
                notification.setHashCode(input.hashCode());
                notification.setCaption(parseInfo.caption);
                ParseContext<Message.Notification> notificationContext = new ParseContext<>(notification);
                parseToContext(input, parseInfo, notificationContext);
                notificationContext.setError(RawQueryEvent.ErrorMessage.okay());

                if (notificationContext.isClosed()) {
                    onNotification(notificationContext.getMessage());
                    return Optional.of(notificationContext.getMessage());
                }

            } else {
                ParseContext<Message.Answer> answerContext = getParseContextFor(parseInfo);
                parseToContext(input, parseInfo, answerContext);

                if (answerContext.isClosed()) {
                    context = null;
                    onAnswer(answerContext.getMessage());
                    return Optional.of(answerContext.getMessage());
                }
            }

            return Optional.empty();

        } catch (Exception ex) {
            throw new QueryParseException("An exception was encountered during parsing.", ex);
        }
    }

    /**
     * Returns the parsing context in regard to the peek information.
     * Determines whether or not to slot in a Notification context or to continue parsing on the current answer context.
     */
    private ParseContext<Message.Answer> getParseContextFor(ParseInfo parseInfo) {
        if (context == null) {
            context = new ParseContext<>(new Message.Answer(internalProvideRequest()));
        }

        if (parseInfo.isError) {
            // This message is an isError message
            Message.ErrorMessage errorMessage = new Message.ErrorMessage(internalProvideRequest());
            context.setError(errorMessage);
        }

        return context;
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
                case Symbols.CHAINDIV:
                    // Flush current key and value
                    parseContext.flushProperty();
                    doKey = true;

                    if (!parseInfo.isError) {
                        Message next;
                        if (parseInfo.isNotification) {
                            next = new Message.Notification();
                            ((Message.Notification) next).setCaption(parseInfo.caption);
                        } else {
                            next = new Message.Answer(internalProvideRequest());
                        }
                        parseContext.nextObject(next);
                    }
                    break;

                case Symbols.PROPDIV:
                    parseContext.flushProperty();
                    doKey = true;
                    break;

                case Symbols.PROPVALDIV:
                    if (doKey) {
                        doKey = false;
                    } else {
                        parseContext.addToValBuffer(c);
                    }
                    break;

                default:
                    if (doKey) {
                        parseContext.addToKeyBuffer(c);
                    } else {
                        parseContext.addToValBuffer(c);
                    }
            }
        }

        parseContext.closeContext();
    }

    private void onNotification(Message.Notification event) {
        if (this.onNotification != null) {
            this.onNotification.accept(event);
        }
    }

    private void onAnswer(Message.Answer event) {
        if (this.onAnswer != null) {
            this.onAnswer.accept(event);
        }
    }

    private IQueryRequest internalProvideRequest() {
        IQueryRequest request = requestSupplier.get();

        if (request == null) {
            throw new QueryParseException("Request may not be null for anwers!");
        }

        return request;
    }

    /**
     * Extracts basic information about the input by peeking into it.
     */
    public static class ParseInfo {
        private String caption;

        private boolean isError;
        private boolean isNotification;

        public String inspect(String input) {
            int firstSpace = input.indexOf(Symbols.PROPDIV);
            int firstEquals = input.indexOf(Symbols.PROPVALDIV);

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
