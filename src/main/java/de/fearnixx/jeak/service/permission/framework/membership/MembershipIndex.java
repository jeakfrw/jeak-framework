package de.fearnixx.jeak.service.permission.framework.membership;

import java.util.List;
import java.util.UUID;

/**
 * Helper class responsible for managing subject memberships and inheritance.
 */
public abstract class MembershipIndex {

    public boolean isParentOf(UUID parent, UUID subjectUUID) {
        return getParentsOf(subjectUUID)
                .stream()
                .anyMatch(existing -> existing.equals(parent));

    }

    public abstract List<UUID> getMembersOf(UUID subjectUUID);

    public abstract List<UUID> getParentsOf(UUID subjectUUID);

    public abstract void addParent(UUID parent, UUID toSubject);

    public abstract void removeParent(UUID parent, UUID fromSubject);

    public void setParent(UUID parent, UUID forSubject) {
        getParentsOf(forSubject).forEach(existing -> removeParent(existing, forSubject));
        addParent(parent, forSubject);
    }
}
