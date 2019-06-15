package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.base.PermissionSubject;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.Optional;
import java.util.UUID;

public class TS3PermissionSubject extends PermissionSubject implements ITS3Subject {

    public TS3PermissionSubject(IPermissionService permissionService, UUID subjectUUID) {
        super(permissionService, subjectUUID);
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return Optional.empty();
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return Optional.empty();
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip) {
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value) {
        return null;
    }

    @Override
    public IQueryRequest unassignPermission(String permSID) {
        return null;
    }
}
