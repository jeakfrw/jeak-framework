package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.framework.FrameworkPermission;
import de.fearnixx.jeak.service.permission.framework.SubjectCache;
import de.fearnixx.jeak.service.permission.framework.membership.MembershipIndex;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.IValueHolder;
import de.mlessmann.confort.api.except.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigSubject extends SubjectAccessor {

    private static final Logger logger = LoggerFactory.getLogger(ConfigSubject.class);

    private final IConfig configRef;
    private boolean dead = false;
    private boolean modified;

    public ConfigSubject(UUID subjectUUID, IConfig configRef, SubjectCache permissionSvc, MembershipIndex membershipIndex) {
        super(subjectUUID, permissionSvc, membershipIndex);
        this.configRef = configRef;
        try {
            configRef.load();
        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Cannot construct configuration subject due to an error!", e);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPermission(permission)
                .map(p -> p.getValue() > 0)
                .orElse(false);
    }

    @Override
    protected Optional<IPermission> getPermissionFromSelf(String permSID) {
        IConfigNode valueNode = configRef.getRoot()
                .getNode("permissions", permSID, "value");

        if (valueNode.isPrimitive()) {
            Integer permValue = valueNode.asInteger();
            FrameworkPermission perm = new FrameworkPermission(permSID, permValue);
            return Optional.of(perm);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Integer> getLinkedServerGroup() {
        return configRef.getRoot()
                .getNode("link")
                .optInteger()
                // 0 or negative links are interpreted as: Not linked.
                .map(i -> i > 0 ? i : null);
    }

    @Override
    public List<IPermission> getPermissions() {
        List<IPermission> permissions = new LinkedList<>();
        configRef.getRoot()
                .getNode("permissions")
                .asMap()
                .forEach((perm, node) -> {
                    IConfigNode valueNode = node.getNode("value");
                    Integer permValue = valueNode.asInteger();
                    permissions.add(new FrameworkPermission(perm, permValue));
                });
        return List.copyOf(permissions);
    }

    @Override
    public boolean setPermission(String permission, int value) {
        configRef.getRoot().getNode("permissions", permission, "value")
                .setInteger(value);
        setModified();
        return true;
    }

    @Override
    public boolean removePermission(String permission) {
        configRef.getRoot().getNode("permissions")
                .remove(permission);
        setModified();
        return true;
    }

    @Override
    public synchronized void saveIfModified() {
        if (isModified() && !configRef.getRoot().isVirtual()) {
            if (this.dead) {
                logger.info("Not saving permission profile: {} - invalidated.", getUniqueID());
                modified = false;
                return;
            }

            try {
                configRef.save();
                logger.debug("Saved profile: {}", getUniqueID());
            } catch (IOException e) {
                logger.warn("Failed to save permission subject configuration!", e);
            }
        } else {
            logger.debug("Not saving. Unmodified or virtual: {}", getUniqueID());
        }
    }

    @Override
    public synchronized void mergeFrom(SubjectAccessor fromSubject) {
        fromSubject.getPermissions().forEach(p -> setPermission(p.getSID(), p.getValue()));
        fromSubject.getMembers().forEach(this::addMember);
        fromSubject.invalidate();
    }

    @Override
    public void invalidate() {
        this.dead = true;
    }

    private synchronized void setModified() {
        modified = true;
    }

    private synchronized boolean isModified() {
        return modified;
    }
}
