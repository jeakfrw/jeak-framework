package de.fearnixx.jeak.service.permission.framework.membership;

import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.IValueHolder;
import de.mlessmann.confort.api.except.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ConfigMembershipIndex extends MembershipIndex {

    private static final Logger logger = LoggerFactory.getLogger(ConfigMembershipIndex.class);
    private IConfig config;
    private boolean modified;

    public ConfigMembershipIndex(IConfig config) {
        this.config = config;
    }

    public boolean load() {
        try {
            config.load();
            if (config.getRoot().isVirtual()) {
                config.getRoot().getNode("parents").setMap();
                setModified();
            }
            return true;
        } catch (IOException | ParseException e) {
            logger.error("Failed to load membership index!", e);
            return false;
        }
    }

    @Override
    public synchronized List<UUID> getMembersOf(UUID subjectUUID) {
        final String sUID = subjectUUID.toString();
        List<UUID> members = new LinkedList<>();
        config.getRoot()
                .getNode("parents")
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((key, value) -> {
                    final boolean isMember = value
                            .optList()
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .map(IValueHolder::asString)
                            .anyMatch(str -> str.equals(sUID));
                    if (isMember) {
                        members.add(UUID.fromString(key));
                    }
                });
        return new ArrayList<>(members);
    }

    @Override
    public synchronized List<UUID> getParentsOf(UUID subjectUUID) {
        List<UUID> parents = new LinkedList<>();
        config.getRoot()
                .getNode("parents", subjectUUID.toString())
                .optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IValueHolder::asString)
                .map(UUID::fromString)
                .forEach(parents::add);
        return new ArrayList<>(parents);
    }

    @Override
    public synchronized void addParent(UUID parent, UUID toSubject) {
        final String parentSUID = parent.toString();
        final IConfigNode subjectNode = config.getRoot().getNode("parents", parentSUID);
        final boolean alreadyAssigned = subjectNode.optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IValueHolder::asString)
                .anyMatch(existing -> existing.equals(parentSUID));

        if (!alreadyAssigned) {
            final IConfigNode entry = subjectNode.createNewInstance();
            entry.setString(parent.toString());
            subjectNode.append(entry);

            setModified();
        }
    }

    @Override
    public void removeParent(UUID parent, UUID fromSubject) {

    }

    private synchronized void setModified() {
        modified = true;
    }

    private synchronized boolean isModified() {
        return modified;
    }

    @Override
    public synchronized void saveIfModified() {
        if (isModified()) {
            try {
                config.save();
                modified = false;
            } catch (IOException e) {
                logger.warn("Failed to save configuration!", e);
            }
        }
    }
}
