package de.fearnixx.t3.teamspeak.query.parser;

import de.fearnixx.t3.event.query.RawQueryEvent;
import de.fearnixx.t3.event.query.RawQueryEvent.Message;
import de.fearnixx.t3.teamspeak.except.QueryParseException;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.fearnixx.t3.event.IRawQueryEvent.IMessage;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public class QueryParser {

    /* Parsing */
    public static class Chars {

        private Chars() {
            // Hide public constructor.
        }

        public static final char PROPDIV = ' ';
        public static final char CHAINDIV = '|';
        public static final char PROPVALDIV = '=';
    }

    /**
     * Instance of the request that's currently running
     * -> Flushed when Message ended
     */
    @Deprecated
    private IQueryRequest currentRequest;
    private final Supplier<IQueryRequest> requestSupplier;

    /**
     * Instance of the currently parsed answer
     * -> Messages are multi-lined ended by the "error"-response
     */
    private ParseContext<Message.Answer> context;


    private final Consumer<IMessage.INotification> onNotification;
    private final Consumer<IMessage.IAnswer> onAnswer;

    public QueryParser(Consumer<IMessage.INotification> onNotification, Consumer<IMessage.IAnswer> onAnswer, Supplier<IQueryRequest> requestSupplier) {
        this.onNotification = onNotification;
        this.onAnswer = onAnswer;
        this.requestSupplier = requestSupplier;
    }

    @Deprecated
    public QueryParser() {
        this.onNotification = null;
        this.onAnswer = null;
        this.requestSupplier = null;
    }

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

            if (parseInfo.isNotification) {
                Message.Notification notification = new Message.Notification();
                notification.setHashCode(input.hashCode());
                notification.setCaption(parseInfo.caption);
                ParseContext<Message.Notification> notificationContext = new ParseContext<>(notification);
                parseToContext(input, parseInfo, notificationContext);
                notificationContext.setError(RawQueryEvent.ErrorMessage.OK());

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
                            next = new Message.Answer(internalProvideRequest());
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

    private void onNotification(Message.Notification event) {
        if (this.onNotification != null)
            this.onNotification.accept(event);
    }

    private void onAnswer(Message.Answer event) {
        if (this.onAnswer != null)
            this.onAnswer.accept(event);
    }

    @SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
    private IQueryRequest internalProvideRequest() {
        IQueryRequest request = requestSupplier != null ? requestSupplier.get() : currentRequest;

        if (request == null) {
            throw new QueryParseException("Request may not be null for anwers!");
        }

        return request;
    }

    @Deprecated
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
