package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TS3UserSubject extends TS3PermissionSubject implements ITS3UserSubject {

    private static final Logger logger = LoggerFactory.getLogger(TS3UserSubject.class);

    private final int clientDBID;

    public TS3UserSubject(ITS3PermissionProvider permissionProvider, int clientDBID) {
        super(permissionProvider);
        this.clientDBID = clientDBID;
    }

    @Override
    public Integer getClientDBID() {
        return clientDBID;
    }

    @Override
    public List<ITS3ServerGroupSubject> getServerGroups() {
        throw new UnsupportedOperationException("Use #getServerGroups(List<Integer>) instead!");
    }

    public List<ITS3ServerGroupSubject> getServerGroups(List<Integer> fromServerGroupIDs) {
        return fromServerGroupIDs.stream()
                .map(id -> new TS3ServerGroupSubject(getPermissionProvider(), id))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return getPermissionProvider().getClientPermission(clientDBID, permSID);
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return getPermissionProvider().getActivePermission(clientDBID, permSID);
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        final Integer permID = getPermissionProvider().translateSID(permSID);
        return IQueryRequest.builder()
                .command(QueryCommands.PERMISSION.CLIENT_PERMISSION_ADD)
                .addKey(PropertyKeys.Client.DBID, clientDBID)
                .addKey(PropertyKeys.Permission.ID, permID)
                .addKey(PropertyKeys.Permission.FLAG_NEGATED, permNegated ? "1" : "0")
                .addKey(PropertyKeys.Permission.FLAG_SKIP, permSkip ? "1" : "0")
                .addKey(PropertyKeys.Permission.VALUE, value)
                .onError(e -> logger.warn("Failed to assign permission \"{}\" to clientDBID \"{}\": {} - {}",
                        permSID, clientDBID, e.getErrorCode(), e.getErrorMessage()))
                .onDone(a -> logger.debug("Assigned permission \"{}\" to clientDBID \"{}\"", permSID, clientDBID))
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
                .command(QueryCommands.PERMISSION.CLIENT_PERMISSION_DEL)
                .addKey(PropertyKeys.Permission.ID, permID)
                .addKey(PropertyKeys.Client.DBID, clientDBID)
                .onError(e -> logger.warn("Failed to unassign permission \"{}\" from clientDBID \"{}\": {} - {}",
                        permSID, clientDBID, e.getErrorCode(), e.getErrorMessage()))
                .onDone(a -> logger.debug("Unassigned permission \"{}\" from clientDBID \"{}\"", permID, clientDBID))
                .build();
    }
}
