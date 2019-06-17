package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.service.permission.framework.InternalPermissionProvider;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Group;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Subject;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Main implementation of {@link ISubject}.
 * This implementation delegates the permission requests to their respective permission providers so they can apply their own logic.
 */
public class PermissionSubject implements ISubject, ITS3Subject {

    private static final Logger logger = LoggerFactory.getLogger(PermissionSubject.class);

    private IPermissionService permissionService;
    private UUID subjectUUID;

    public PermissionSubject(IPermissionService permissionService, UUID subjectUUID) {
        this.permissionService = permissionService;
        this.subjectUUID = subjectUUID;
    }

    // == Abstract subject == //

    public UUID getUniqueID() {
        return subjectUUID;
    }

    public List<ITS3Group> getServerGroups() {
        return Collections.emptyList();
    }

    @Override
    public List<IGroup> getParents(String systemID) {
        Optional<IPermissionProvider> provider = permissionService.provide(systemID;
        if (provider.isEmpty()) {
            logger.info("Provider was requested but is not present: {}", systemID);
            return Collections.emptyList();
        } else {
            return provider.get().getParentsOf(getUniqueID());
        }
    }

    @Override
    public List<IGroup> getParents() {
        return permissionService.getFrameworkProvider().getParentsOf(getUniqueID());
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPermission(permission)
                .map(p -> p.getValue() > 0)
                .orElse(false);
    }

    public Optional<IPermission> getPermission(String permission) {
        String systemId;
        int colonIndex = permission.indexOf(":");
        if (colonIndex >= 0) {
            // System ID present.
            systemId = permission.substring(0, colonIndex);
            permission = permission.substring(colonIndex + 1);
        } else {
            systemId = InternalPermissionProvider.SYSTEM_ID;
        }

        Optional<IPermissionProvider> provider = permissionService.provide(systemId);
        if (provider.isEmpty()) {
            logger.info("Provider was requested but is not present: {}", systemId);
            return Optional.empty();
        } else {
            return provider.get().getPermission(permission, getUniqueID());
        }
    }

    @Override
    public List<IPermission> getPermissions(String systemId) {
        Optional<IPermissionProvider> provider = permissionService.provide(systemId);
        if (provider.isEmpty()) {
            logger.info("Provider was requested but is not present: {}", systemId);
            return Collections.emptyList();
        } else {
            return provider.get().listPermissions(getUniqueID());
        }
    }

    @Override
    public List<IPermission> getPermissions() {
        return getPermissions(InternalPermissionProvider.SYSTEM_ID);
    }

    @Override
    public void setPermission(String permission, int value) {
        String systemId;
        int colonIndex = permission.indexOf(":");
        if (colonIndex >= 0) {
            // System ID present.
            systemId = permission.substring(0, colonIndex);
            permission = permission.substring(colonIndex + 1);
        } else {
            systemId = InternalPermissionProvider.SYSTEM_ID;
        }

        Optional<IPermissionProvider> provider = permissionService.provide(systemId);
        if (provider.isEmpty()) {
            logger.warn("Provider was requested but is not present: {}", systemId);
        } else {
            provider.get().setPermission(permission, getUniqueID(), value);
        }
    }

    @Override
    public void removePermission(String permission) {
        String systemId;
        int colonIndex = permission.indexOf(":");
        if (colonIndex >= 0) {
            // System ID present.
            systemId = permission.substring(0, colonIndex);
            permission = permission.substring(colonIndex + 1);
        } else {
            systemId = InternalPermissionProvider.SYSTEM_ID;
        }

        Optional<IPermissionProvider> provider = permissionService.provide(systemId);
        if (provider.isEmpty()) {
            logger.warn("Provider was requested but is not present: {}", systemId);
        } else {
            provider.get().removePermission(permission, getUniqueID());
        }
    }

    // == TeamSpeak 3 subject == //


    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return permissionService.getTS3Provider()
                .getActivePermission(getUniqueID(), permSID);
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

    @Override
    public ITS3Group getChannelGroup() {
        return null;
    }
}

