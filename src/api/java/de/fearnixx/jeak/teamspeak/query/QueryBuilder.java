package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * (Name shortened from `QueryRequestBuilder` for readability reasons).
 * Constructs a new object implementing {@link IQueryRequest} using the builder-pattern.
 *
 * @see IQueryRequest for the actual meanings of the methods.
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class QueryBuilder {

    private static final Boolean WARN_CB_REPLACE = Main.getProperty("jeak.checks.queryCBReplace", true);
    public static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

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
        currentObj = null;
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
     *
     * @param key   The key
     * @param value String|Object - on objects {@link #toString()} is invoked
     */
    public QueryBuilder addKey(String key, Object value) {
        if (currentObj.hasProperty(key))
            currentObj.setProperty(key, value != null ? value.toString() : null);
        else
            currentObj.setProperty(key, value != null ? value.toString() : null);
        return this;
    }

    /**
     * Convenience method to avoid having to call {@link TargetType#getQueryNum()} manually.
     */
    public QueryBuilder addKey(String key, TargetType targetType) {
        return addKey(key, targetType.getQueryNum());
    }

    public QueryBuilder addOption(String option) {
        options.add(option);
        return this;
    }

    public QueryBuilder onDone(Consumer<IQueryEvent.IAnswer> callback) {
        if (WARN_CB_REPLACE && this.onDone != null) {
            logger.warn("Replacing on-done callback. Did you mean to register the CB on the request?");
        }

        this.onDone = callback;
        return this;
    }

    public QueryBuilder onError(Consumer<IQueryEvent.IAnswer> callback) {
        if (WARN_CB_REPLACE && this.onError != null) {
            logger.warn("Replacing on-error callback. Did you mean to register the CB on the request?");
        }

        this.onError = callback;
        return this;
    }

    public QueryBuilder onSuccess(Consumer<IQueryEvent.IAnswer> callback) {
        if (WARN_CB_REPLACE && this.onSuccess != null) {
            logger.warn("Replacing on-success callback. Did you mean to register the CB on the request?");
        }

        this.onSuccess = callback;
        return this;
    }

    public IQueryRequest build() {
        final List<Consumer<IQueryEvent.IAnswer>> onDoneCBs = new LinkedList<>();
        final List<Consumer<IQueryEvent.IAnswer>> onSuccessCBs = new LinkedList<>();
        final List<Consumer<IQueryEvent.IAnswer>> onErrorCBs = new LinkedList<>();

        if (onDone != null) {
            onDoneCBs.add(onDone);
        }
        if (onSuccess != null) {
            onSuccessCBs.add(onSuccess);
        }
        if (onError != null) {
            onErrorCBs.add(onError);
        }

        return new IQueryRequest() {
            final String fComm = command;
            final List<IDataHolder> fChain = Collections.unmodifiableList(chain);
            final List<String> fOptions = Collections.unmodifiableList(options);

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
                return (event) -> onDoneCBs.forEach(callback -> callback.accept(event));
            }

            @Override
            public void onDone(Consumer<IQueryEvent.IAnswer> onDoneConsumer) {
                Objects.requireNonNull(onDoneConsumer, "Cannot register null-callback!");
                onDoneCBs.add(onDoneConsumer);
            }

            @Override
            public Consumer<IQueryEvent.IAnswer> onError() {
                return (event) -> onErrorCBs.forEach(callback -> callback.accept(event));
            }

            @Override
            public void onError(Consumer<IQueryEvent.IAnswer> onErrorConsumer) {
                Objects.requireNonNull(onErrorConsumer, "Cannot register null-callback!");
                onErrorCBs.add(onErrorConsumer);
            }

            @Override
            public Consumer<IQueryEvent.IAnswer> onSuccess() {
                return (event) -> onSuccessCBs.forEach(callback -> callback.accept(event));
            }

            @Override
            public void onSuccess(Consumer<IQueryEvent.IAnswer> onSuccessConsumer) {
                Objects.requireNonNull(onSuccessConsumer, "Cannot register null-callback!");
                onSuccessCBs.add(onSuccessConsumer);
            }
        };
    }
}
