package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.IValueHolder;
import de.mlessmann.confort.api.except.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ConfigSubject extends SubjectAccessor {

    private static final Logger logger = LoggerFactory.getLogger(ConfigSubject.class);

    private final IConfig configRef;
    private boolean modified;

    public ConfigSubject(UUID subjectUUID, IConfig configRef, SubjectCache permissionSvc) {
        super(subjectUUID, permissionSvc);
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
    public Optional<IPermission> getPermission(String permSID) {
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
    public List<IGroup> getParents() {
        return configRef.getRoot()
                .getNode("parents")
                .optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IValueHolder::asString)
                .map(UUID::fromString)
                .map(this.getCache()::getSubject)
                .collect(Collectors.toList());
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
    public List<UUID> getMemberSubjects() {
        return configRef.getRoot()
                .getNode("members")
                .optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IValueHolder::asString)
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    @Override
    public void setPermission(String permission, int value) {
        configRef.getRoot().getNode("permissions", permission, "value")
                .setInteger(value);
        setModified();
    }

    @Override
    public void removePermission(String permission) {
        configRef.getRoot().getNode("permissions")
                .remove(permission);
        setModified();
    }

    @Override
    public synchronized void saveIfModified() {
        if (isModified()) {
            try {
                configRef.save();
            } catch (IOException e) {
                logger.warn("Failed to save permission subject configuration!", e);
            }
        }
    }

    @Override
    public synchronized void mergeInto(UUID into) {
        SubjectAccessor intoSubject = getCache().getSubject(into);
    }

    private synchronized void setModified() {
        modified = true;
    }

    private synchronized boolean isModified() {
        return modified;
    }
}
