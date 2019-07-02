package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TS3ServerGroupSubject extends TS3PermissionSubject implements ITS3ServerGroupSubject {

    private static final Logger logger = LoggerFactory.getLogger(TS3ServerGroupSubject.class);

    private final int serverGroupID;

    public TS3ServerGroupSubject(ITS3PermissionProvider permissionProvider, int serverGroupID) {
        super(permissionProvider);
        this.serverGroupID = serverGroupID;
    }

    @Override
    public Integer getServerGroupID() {
        return serverGroupID;
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return getPermissionProvider().getServerGroupPermission(serverGroupID, permSID);
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return getTS3Permission(permSID);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        final Integer permID = getPermissionProvider().translateSID(permSID);
        return IQueryRequest.builder()
                .command(QueryCommands.PERMISSION.SERVERGROUP_ADD_PERMISSION)
                .addKey("sgid", serverGroupID)
                .addKey(PropertyKeys.Permission.ID, permID)
                .addKey(PropertyKeys.Permission.VALUE, value)
                .addKey(PropertyKeys.Permission.FLAG_SKIP, permSkip ? "1" : "0")
                .addKey(PropertyKeys.Permission.FLAG_NEGATED, permNegated ? "1" : "0")
                .onError(e -> logger.warn("Failed to assign permission \"{}\" to server group \"{}\": {} - {}",
                        permSID, serverGroupID, e.getErrorCode(), e.getErrorMessage()))
                .onSuccess(a -> logger.warn("Assigned permission \"{}\" to server group \"{}\".", permSID, serverGroupID))
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
                .command(QueryCommands.PERMISSION.SERVERGROUP_DEL_PERMISSION)
                .addKey("sgid", serverGroupID)
                .addKey(PropertyKeys.Permission.ID, permID)
                .onError(e -> logger.warn("Failed to revoke permission \"{}\" for server group \"{}\": {} - {}",
                        permSID, serverGroupID, e.getErrorCode(), e.getErrorMessage()))
                .onSuccess(a -> logger.debug("Revoked permission \"{}\" for server group \"{}\".", permSID, serverGroupID))
                .build();
    }
}
