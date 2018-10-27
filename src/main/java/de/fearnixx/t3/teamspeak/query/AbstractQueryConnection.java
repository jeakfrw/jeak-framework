package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.data.IDataHolder;

import java.util.function.Consumer;

public abstract class AbstractQueryConnection implements IQueryConnection {

    @Override
    public boolean blockingLogin(Integer instanceID, String user, String pass) {
        return false;
    }

    @Override
    public IDataHolder getWhoAmI() {
        return null;
    }

    @Override
    public Integer getInstanceID() {
        return null;
    }

    @Override
    public void setNickName(String nickName) {
        IQueryRequest request = IQueryRequest.builder()
                .command("clientupdate")
                .addKey(PropertyKeys.Client.NICKNAME, nickName)
                .build();

        sendRequest(request);
    }

    @Override
    public void sendRequest(IQueryRequest req, Consumer<IRawQueryEvent.IMessage.IAnswer> onDone) {
        Consumer<IQueryEvent.IAnswer> rDone = answer ->
                onDone.accept(((IRawQueryEvent.IMessage.IAnswer) answer.getRawReference()));

        if (req.onDone() != null) {
            rDone = rDone.andThen(req.onDone());
        }

        IQueryRequest queryRequest = QueryBuilder.from(req)
                .onDone(rDone)
                .build();
        sendRequest(queryRequest);
    }

    @Override
    public IQueryPromise promiseRequest(IQueryRequest request) {
        final PromisedRequest promise = new PromisedRequest(request);
        sendRequest(request, promise::setAnswer);
        return promise;
    }
}
