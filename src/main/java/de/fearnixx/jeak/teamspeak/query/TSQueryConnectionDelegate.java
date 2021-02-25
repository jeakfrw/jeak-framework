package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.api.ITSQueryConnection;
import de.fearnixx.jeak.util.URIContainer;

import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Delegate class to mixin the frameworks {@link IQueryConnection} contract into the internal library classes.
 *
 * @since 1.2.0
 */
public class TSQueryConnectionDelegate implements IQueryConnection, ITSQueryConnection {

    private final ITSQueryConnection delegate;

    public TSQueryConnectionDelegate(TSQueryConnection delegate) {
        this.delegate = delegate;
        delegate.setDelegationTarget(this);
    }

    public URIContainer getURI() {
        return ((TSQueryConnection) delegate).getURI();
    }

    @Override
    public IDataHolder getWhoAmI() {
        return ((TSQueryConnection) delegate).getWhoAmIResponse()
                .orElseThrow(() -> new IllegalStateException("WhoAmI now known yet!"));
    }

    @Override
    public void setNickName(String nickName) {
        IQueryRequest request = IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_UPDATE)
                .addKey(PropertyKeys.Client.NICKNAME, nickName)
                .build();

        sendRequest(request);
    }

    @Override
    public void sendRequest(IQueryRequest req) {
        queueRequest(req);
    }

    @Override
    public void queueRequest(IQueryRequest request) {
        delegate.queueRequest(request);
    }

    @Override
    public Future<IQueryEvent.IAnswer> promiseRequest(IQueryRequest request) {
        return delegate.promiseRequest(request);
    }

    @Override
    public void onNotification(Consumer<IQueryEvent.INotification> notificationConsumer) {
        delegate.onNotification(notificationConsumer);
    }

    @Override
    public void onAnswer(Consumer<IQueryEvent.IAnswer> answerConsumer) {
        delegate.onAnswer(answerConsumer);
    }

    @Override
    public void onClosed(BiConsumer<ITSQueryConnection, Boolean> closeConsumer) {
        delegate.onClosed(closeConsumer);
    }

    @Override
    public void lockListeners(String reason) {
        delegate.lockListeners(reason);
    }

    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public void run() {
        delegate.run();
    }

    @Override
    public boolean isClosed() {
        return !delegate.isActive();
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }
}
