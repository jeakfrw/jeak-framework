package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * (Name shortened from `QueryRequestBuilder` for readability reasons).
 *
 * Constructs a new object implementing {@link IQueryRequest} using the builder-pattern.
 * @see IQueryRequest for the actual meanings of the methods.
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class QueryBuilder {

    public static QueryBuilder from(IQueryRequest request) {
        QueryBuilder builder = new QueryBuilder();

        builder.command(request.getCommand());
        request.getDataChain().forEach(builder::appendToChain);
        request.getOptions().forEach(builder::addOption);

        builder.onDone(request.onDone());
        builder.onError(request.onError());
        builder.onSuccess(request.onSuccess());
        return builder;
    }

    private String command;
    private IDataHolder currentObj;
    private List<IDataHolder> chain;
    private List<String> options;
    private Consumer<IQueryEvent.IAnswer> onDone;
    private Consumer<IQueryEvent.IAnswer> onError;
    private Consumer<IQueryEvent.IAnswer> onSuccess;

    QueryBuilder() {
        reset();
    }

    public QueryBuilder reset() {
        command = "";
        currentObj =  null;
        chain = new ArrayList<>();
        options = new ArrayList<>();
        commitChainElement();
        return this;
    }

    public QueryBuilder command(String command) {
        this.command = command;
        return this;
    }

    public QueryBuilder appendToChain(IDataHolder dataHolder) {
        chain.remove(chain.size() - 1);
        chain.add(new BasicDataHolder().copyFrom(dataHolder));
        commitChainElement();
        return this;
    }

    public QueryBuilder commitChainElement() {
        chain.add(new BasicDataHolder());
        currentObj = chain.get(chain.size() - 1);
        return this;
    }

    /**
     * @deprecated misleading name - use {@link #commitChainElement()}
     */
    @Deprecated
    public QueryBuilder newChain() {
        return commitChainElement();
    }

    /**
     * Add a parameter to the current chain.
     * @param key The key
     * @param value String|Object - on objects {@link #toString()} is invoked
     */
    public QueryBuilder addKey(String key, Object value) {
        if (currentObj.hasProperty(key))
            currentObj.setProperty(key, value != null ? value.toString() : null);
        else
            currentObj.setProperty(key, value != null ? value.toString() : null);
        return this;
    }

    public QueryBuilder addOption(String option) {
        options.add(option);
        return this;
    }

    public QueryBuilder onDone(Consumer<IQueryEvent.IAnswer> callback) {
        this.onDone = callback;
        return this;
    }

    public QueryBuilder onError(Consumer<IQueryEvent.IAnswer> callback) {
        this.onError = callback;
        return this;
    }

    public QueryBuilder onSuccess(Consumer<IQueryEvent.IAnswer> callback) {
        this.onSuccess = callback;
        return this;
    }

    public IQueryRequest build() {
        return new IQueryRequest() {
            final String fComm = command;
            final List<IDataHolder> fChain = Collections.unmodifiableList(chain);
            final List<String> fOptions = Collections.unmodifiableList(options);
            final Consumer<IQueryEvent.IAnswer> fOnDone = onDone;
            final Consumer<IQueryEvent.IAnswer> fOnError = onError;
            final Consumer<IQueryEvent.IAnswer> fOnSuccess = onSuccess;

            @Override
            public String getCommand() {
                return fComm;
            }

            @Override
            public List<Map<String, String>> getChain() {
                return fChain.stream()
                        .map(IDataHolder::getValues)
                        .collect(Collectors.toList());
            }

            @Override
            public List<IDataHolder> getDataChain() {
                return fChain;
            }

            @Override
            public List<String> getOptions() {
                return fOptions;
            }

            @Override
            public Consumer<IQueryEvent.IAnswer> onDone() {
                return fOnDone;
            }

            @Override
            public Consumer<IQueryEvent.IAnswer> onError() {
                return fOnError;
            }

            @Override
            public Consumer<IQueryEvent.IAnswer> onSuccess() {
                return fOnSuccess;
            }
        };
    }
}
