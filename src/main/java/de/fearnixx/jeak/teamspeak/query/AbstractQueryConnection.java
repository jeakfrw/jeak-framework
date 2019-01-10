package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

import java.util.function.Consumer;

public abstract class AbstractQueryConnection implements IQueryConnection {

    protected final IQueryRequest whoAmIRequest =
            IQueryRequest.builder()
                    .command("whoami")
                    .onSuccess(answer -> whoamiAnswer = answer.getDataChain().get(0))
                    .build();

    private IDataHolder whoamiAnswer;

    @Override
    public IDataHolder getWhoAmI() {
        return whoamiAnswer;
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
