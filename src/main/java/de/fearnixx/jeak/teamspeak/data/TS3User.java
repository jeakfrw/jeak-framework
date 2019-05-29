package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;

import java.util.Arrays;

public class TS3User extends TS3UserHolder {

    @Override
    public IQueryRequest addServerGroup(Integer... serverGroupIds) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.SERVER_GROUP.SERVERGROUP_ADD_CLIENT)
                .addKey("cldbid", this.getClientDBID());
        Arrays.stream(serverGroupIds).forEach(id -> {
            queryBuilder.addKey("sgid", id).commitChainElement();
        });
        return queryBuilder.build();
    }

    @Override
    public IQueryRequest removeServerGroup(Integer... serverGroupIds) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.SERVER_GROUP.SERVERGROUP_DEL_CLIENT)
                .addKey("cldbid", this.getClientDBID());
        Arrays.stream(serverGroupIds).forEach(id -> {
            queryBuilder.addKey("sgid", id).commitChainElement();
        });
        return queryBuilder.build();
    }

    @Override
    public IQueryRequest setChannelGroup(Integer channelId, Integer channelGroupId) {
        return IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_SET_CHANNEL_GROUP)
                .addKey("cldbid", this.getClientDBID())
                .addKey(PropertyKeys.Channel.ID, channelId)
                .addKey("cgid", channelGroupId)
                .build();
    }
}
