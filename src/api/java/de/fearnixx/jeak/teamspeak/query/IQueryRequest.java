package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

import java.util.List;
import java.util.function.Consumer;

/**
 * Framework representation for TeamSpeak query commands.
 */
public interface IQueryRequest {

    /**
     * Create a new request builder.
     */
    static QueryBuilder builder() {
        return new QueryBuilder();
    }

    /**
     * TeamSpeak query command.
     * A query will not be sent without having a command set.
     */
    String getCommand();

    /**
     * TS3 allows chaining for most commands to run one command with multiple properties at once.
     * We will support that by making this a list.
     * Check if chaining is supported for your requested command during development.
     * If not, just fill the first element in the chain - only that will be sent in that case.
     *
     * The data properties to send with the request.
     */
    List<IDataHolder> getDataChain();

    /**
     * Some commands may be appended with switch-like options.
     * These are appended to the query after the last chain element.
     */
    List<String> getOptions();

    /**
     * When the request has been sent and a complete response has been received from TeamSpeak,
     * the {@link IQueryConnection} invokes some callbacks based on the response.
     *
     * @implNote Consider callbacks asynchronous.
     */
    Consumer<IQueryEvent.IAnswer> onDone();

    /**
     * Attach an additional callback to this request.
     */
    void onDone(Consumer<IQueryEvent.IAnswer> onDoneConsumer);

    /**
     * {@link #onDone()} for responses with {@code error = 0}.
     */
    Consumer<IQueryEvent.IAnswer> onSuccess();

    /**
     * Attach an additional callback to this request.
     */
    void onSuccess(Consumer<IQueryEvent.IAnswer> onSuccessConsumer);

    /**
     * {@link #onDone()} for responses with {@code error != 0}
     */
    Consumer<IQueryEvent.IAnswer> onError();

    /**
     * Attach an additional callback to this request.
     */
    void onError(Consumer<IQueryEvent.IAnswer> onErrorConsumer);
}
