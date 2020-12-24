package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.api.ITSMessageChannel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.2.0
 * @deprecated Wrapper class to implement the deprecated {@link IQueryConnection} contract. Will be removed when the contract is removed.
 */
@Deprecated(since = "1.2.0")
public class TSQueryConnectionWrapper extends TSQueryConnection implements IQueryConnection {

    private final AtomicReference<IDataHolder> whoamiAnswer = new AtomicReference<>();
    private final IQueryRequest whoAmIRequest =
            IQueryRequest.builder()
                    .command(QueryCommands.WHOAMI)
                    .onSuccess(answer -> whoamiAnswer.set(answer.getDataChain().get(0)))
                    .build();

    public TSQueryConnectionWrapper(ITSMessageChannel messageChannel, MessageMarshaller marshaller) {
        super(messageChannel, marshaller);
    }

    @Override
    public IDataHolder getWhoAmI() {
        return Optional.ofNullable(whoamiAnswer.get())
                .orElseThrow(() -> new IllegalStateException("WhoAmI-Response not known yet."));
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
    public boolean isClosed() {
        return !isActive();
    }

    public IQueryRequest getWhoAmIRequest() {
        return whoAmIRequest;
    }
}
