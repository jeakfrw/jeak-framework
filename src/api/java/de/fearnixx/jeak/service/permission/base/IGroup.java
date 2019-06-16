package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.teamspeak.data.IUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Allows grouping sets of permissions together and applying them all it once to {@link ISubject}s.
 */
public interface IGroup extends ISubject {

    /**
     * When set, this will cause all members of the TeamSpeak server group to become members of this group automatically.
     * They cannot be removed from the group unless they are removed from the server group or the link is removed.
     */
    Optional<Integer> getLinkedServerGroup();

    /**
     * All subjects that directly inherit permissions from this group.
     *
     * @apiNote this does not include clients who are only member of this group due to a server group linkage.
     */
    List<UUID> getMembers();

    /**
     * Adds the given UUID as a parent to this subject.
     * <p>
     * Returns {@code false} when the operations is not permitted.
     * This could be the case when the system is read-only or this group cannot have the given subject as a member.
     * </p>
     * <p>
     * Returns {@code true} when the operation was successful.
     * </p>
     *
     * @apiNote This cannot be used for TeamSpeak permissions. Use {@link IUser#addServerGroup(Integer...)} instead.
     */
    boolean addMember(UUID uuid);

    /**
     * Short-hand for {@link #addMember(UUID)}.
     */
    boolean addMember(ISubject subject);
}
