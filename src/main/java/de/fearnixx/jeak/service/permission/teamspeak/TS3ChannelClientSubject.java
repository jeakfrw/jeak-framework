package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TS3ChannelClientSubject extends TS3PermissionSubject implements ITS3ChannelClientSubject {

    private static final Logger logger = LoggerFactory.getLogger(TS3ChannelClientSubject.class);

    private final int clientDBID;
    private final int channelID;

    public TS3ChannelClientSubject(ITS3PermissionProvider permissionProvider, int clientDBID, int channelID) {
        super(permissionProvider);
        this.clientDBID = clientDBID;
        this.channelID = channelID;
    }

    @Override
    public Integer getClientDBID() {
        return clientDBID;
    }

    @Override
    public Integer getChannelID() {
        return channelID;
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return getPermissionProvider().getChannelClientPermission(channelID, clientDBID, permSID);
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return getTS3Permission(permSID);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        final Integer permID = getPermissionProvider().translateSID(permSID);
        return IQueryRequest.builder()
                .command(QueryCommands.PERMISSION.CHANNEL_CLIENT_PERMISSION_ADD)
                .addKey(PropertyKeys.Client.DBID, clientDBID)
                .addKey(PropertyKeys.Channel.ID, channelID)
                .addKey(PropertyKeys.Permission.ID, permID)
                .addKey(PropertyKeys.Permission.VALUE, value)
                .addKey(PropertyKeys.Permission.FLAG_SKIP, permSkip ? "1" : "0")
                .addKey(PropertyKeys.Permission.FLAG_NEGATED, permNegated ? "1" : "0")
                .onError(e -> logger.warn("Failed to assign permission \"{}\" on channel \"{}\" for client \"{}\": {} - {}",
                        permSID, channelID, clientDBID, e.getErrorCode(), e.getErrorMessage()))
                .onDone(a -> logger.debug("Assigned permission \"{}\" on channel \"{}\" for client \"{}\".",
                        permSID, channelID, clientDBID))
                .build();
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip) {
        return assignPermission(permSID, value, permSkip, false);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value) {
        return assignPermission(permSID, value, false);
    }

    @Override
    public IQueryRequest revokePermission(String permSID) {
        final Integer permID = getPermissionProvider().translateSID(permSID);
        return IQueryRequest.builder()
                .command(QueryCommands.PERMISSION.CHANNEL_CLIENT_PERMISSION_DEL)
                .addKey(PropertyKeys.Client.DBID, clientDBID)
                .addKey(PropertyKeys.Channel.ID, channelID)
                .addKey(PropertyKeys.Permission.ID, permID)
                .onError(e -> logger.warn("Failed to revoke permission \"{}\" on channel \"{}\" for client \"{}\" - {} - {}",
                        permSID, channelID, clientDBID, e.getErrorCode(), e.getErrorMessage()))
                .onSuccess(a -> logger.debug("Revoked permission \"{}\" on channel \"{}\" for client \"{}\"",
                        permSID, channelID, clientDBID))
                .build();
    }
}
