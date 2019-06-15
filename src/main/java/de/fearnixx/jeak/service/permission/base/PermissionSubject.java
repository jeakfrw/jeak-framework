package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.service.permission.framework.InternalPermissionProvider;
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
public class PermissionSubject implements ISubject {

    private static final Logger logger = LoggerFactory.getLogger(PermissionSubject.class);

    private IPermissionService permissionService;
    private UUID subjectUUID;

    public PermissionSubject(IPermissionService permissionService, UUID subjectUUID) {
        this.permissionService = permissionService;
        this.subjectUUID = subjectUUID;
    }

    public UUID getUniqueID() {
        return subjectUUID;
    }

    @Override
    public List<IGroup> getParents() {
        return Collections.emptyList();
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
}

