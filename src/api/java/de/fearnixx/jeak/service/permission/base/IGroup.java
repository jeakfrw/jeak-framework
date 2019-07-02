package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Allows grouping sets of permissions together and applying them all it once to {@link ISubject}s.
 */
public interface IGroup extends ISubject {

    /**
     * User-friendly name of this group.
     * If for some reason, the permission system does not support group names by any means, this can be the serialized UUID.
     */
    String getName();

    /**
     * When set, this will cause all members of the TeamSpeak server group to become members of this group automatically.
     * They cannot be removed from the group unless they are removed from the server group or the link is removed.
     *
     * @apiNote For permission systems that do not support this type of connection, this will always be empty.
     * @implNote This feature is restricted to calls on {@link IUser#getPermission(String)} and {@link IClient#getPermission(String)}
     *           as any other implementation would require empty profiles to be persisted just for lookup purposes!
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

    /**
     * <p>
     *      Instructs the underlying system to link the specified server group ID to this group.
     *      This will cause all clients of that server group to automatically inherit from this group as if it was a subject parent.
     *      However, this does not actually add this group to their parents which means that individual users cannot be removed from linked groups
     *      without being removed from the server group as well.
     * </p>
     * <p>
     *     Returns {@code false} for systems/providers that do not support linking server groups.
     *     Note: This is not the same as being a read-only system as this is not dependent on changes in the remote system due to its passive nature.
     * </p>
     */
    boolean linkServerGroup(int serverGroupID);
}
