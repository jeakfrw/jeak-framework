package de.fearnixx.jeak.teamspeak.data;

import de.fearnixx.jeak.teamspeak.KickType;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Behavioral class for cached clients.
 * For data layer, see {@link TS3ClientHolder}
 */
public class TS3Client extends TS3ClientHolder {

    @Override
    public IQueryRequest sendMessage(String message) {
        return IQueryRequest.builder()
                .command(QueryCommands.TEXTMESSAGE_SEND)
                .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.CLIENT)
                .addKey(PropertyKeys.TextMessage.TARGET_ID, this.getClientID())
                .addKey(PropertyKeys.TextMessage.MESSAGE, message)
                .build();
    }

    @Override
    public IQueryRequest sendPoke(String message) {
        return IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_POKE)
                .addKey(PropertyKeys.Client.ID, this.getClientID())
                .addKey(PropertyKeys.TextMessage.MESSAGE, message)
                .build();
    }

    @Override
    public IQueryRequest edit(Map<String, String> properties) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_EDIT)
                .addKey(PropertyKeys.Client.ID, this.getClientID());
        properties.forEach(queryBuilder::addKey);
        return queryBuilder.build();

    }

    @Override
    public IQueryRequest setDescription(String clientDescription) {
        return edit(Collections.singletonMap(PropertyKeys.Client.DESCRIPTION, clientDescription));
    }

    @Override
    public IQueryRequest moveToChannel(Integer channelId) {
        return IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_MOVE)
                .addKey(PropertyKeys.Client.ID, this.getClientID())
                .addKey(PropertyKeys.Channel.ID, channelId)
                .build();
    }

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

    @Override
    public IQueryRequest kickFromServer(String reasonMessage) {
        return kick(KickType.FROM_SERVER, reasonMessage);
    }

    @Override
    public IQueryRequest kickFromServer() {
        return kickFromServer("Kicked.");
    }

    @Override
    public IQueryRequest kickFromChannel(String reasonMessage) {
        return kick(KickType.FROM_CHANNEL, reasonMessage);
    }

    @Override
    public IQueryRequest kickFromChannel() {
        return kickFromChannel("Kicked.");
    }

    private IQueryRequest kick(KickType kickType, String reasonMessage) {
        return IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_KICK)
                .addKey(PropertyKeys.Client.ID, this.getClientID())
                .addKey("reasonmsg", reasonMessage)
                .addKey("reasonid", kickType.getQueryNum())
                .build();
    }

    @Override
    public IQueryRequest ban(String reasonMessage, TimeUnit durationUnit, Integer duration) {
        QueryBuilder queryBuilder = IQueryRequest.builder()
                .command(QueryCommands.BAN.BAN_CLIENT)
                .addKey("banreason", reasonMessage)
                .addKey(PropertyKeys.Client.ID, this.getClientID());

        int banTime = ((int) Math.ceil(durationUnit.toSeconds(duration)));
        if (banTime > 0) {
            queryBuilder.addKey("time", banTime);
        }

        return queryBuilder.build();
    }

    @Override
    public IQueryRequest ban(String reasonMessage, Integer durationInSeconds) {
        return ban(reasonMessage, TimeUnit.SECONDS, durationInSeconds);
    }

    @Override
    public IQueryRequest banPermanent(String reasonMessage) {
        return ban(reasonMessage, TimeUnit.SECONDS, 0);
    }
}