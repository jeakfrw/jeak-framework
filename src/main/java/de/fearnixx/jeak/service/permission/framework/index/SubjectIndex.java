package de.fearnixx.jeak.service.permission.framework.index;

import de.fearnixx.jeak.service.permission.base.IGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Helper class responsible for managing subject indexing.
 */
public abstract class SubjectIndex {

    public boolean isParentOf(UUID parent, UUID subjectUUID) {
        return getParentsOf(subjectUUID)
                .stream()
                .anyMatch(existing -> existing.equals(parent));

    }

    public abstract void linkServerGroup(IGroup group, int serverGroupID);

    public abstract boolean isAdmin(UUID subjectUUID);

    public abstract List<UUID> getMembersOf(UUID subjectUUID);

    public abstract List<UUID> getParentsOf(UUID subjectUUID);

    public abstract void addParent(UUID parent, UUID toSubject);

    public abstract void removeParent(UUID parent, UUID fromSubject);

    public void setParent(UUID parent, UUID forSubject) {
        getParentsOf(forSubject).forEach(existing -> removeParent(existing, forSubject));
        addParent(parent, forSubject);
    }

    public abstract UUID createGroup(String name);

    public abstract void deleteSubject(UUID uniqueId);

    public abstract void saveIfModified();

    public abstract Optional<UUID> findGroupByName(String name);

    public abstract List<UUID> getGroupsLinkedTo(Integer ts3GroupId);
}
