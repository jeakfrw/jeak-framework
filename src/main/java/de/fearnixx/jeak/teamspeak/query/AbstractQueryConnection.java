package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

public abstract class AbstractQueryConnection implements IQueryConnection {

    protected final IQueryRequest whoAmIRequest =
            IQueryRequest.builder()
                    .command(QueryCommands.WHOAMI)
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
                .command(QueryCommands.CLIENT.CLIENT_UPDATE)
                .addKey(PropertyKeys.Client.NICKNAME, nickName)
                .build();

        sendRequest(request);
    }
}
