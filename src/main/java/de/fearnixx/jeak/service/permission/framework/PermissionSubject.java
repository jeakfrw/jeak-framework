package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class PermissionSubject implements ISubject {

    private static final Logger logger = LoggerFactory.getLogger(PermissionSubject.class);

    private IPermissionService permissionService;
    private UUID subjectUUID;

    public PermissionSubject(UUID subjectUUID) {
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
            systemId = FrameworkPermissionService.SYSTEM_ID;
        }

        Optional<IPermissionProvider> provider = permissionService.provide(systemId);
        if (provider.isEmpty()) {
            logger.info("Provider was requested but is not present: {}", systemId);
            return Optional.empty();
        } else {
            return provider.get().getPermission(permission, getUniqueID());
        }
    }

    public abstract SubjectType getType();
}
