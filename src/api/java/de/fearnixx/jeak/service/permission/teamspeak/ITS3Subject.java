package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.List;
import java.util.Optional;

/**
 * Special subject class for TeamSpeak 3 permission system subjects.
 */
public interface ITS3Subject {

    /**
     * Retrieves given ts3 permission via. {@link ITS3PermissionProvider}.
     * @implNote this only retrieves the permission explicitly set to this subject and does not evaluate the whole context.
     */
    Optional<ITS3Permission> getTS3Permission(String permSID);

    /**
     * Retrieves the given ts3 permission via. {@link ITS3PermissionProvider}.
     * Contrary to {@link #getTS3Permission(String)}, this evaluates the TS3 context including inheritance, negated and skipped permissions.
     * @implNote This will only differ from its counterpart for clients as other TS3 subjects do not inherit context.
     */
    Optional<ITS3Permission> getActiveTS3Permission(String permSID);

    /**
     * Returns a query request that will tell the TeamSpeak server to assign this permission to this subject.
     * @apiNote Just like with framework permissions, setting a permission to 0 is not the same as removing it in TS3.
     */
    IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated);

    /**
     * @see #assignPermission(String, int, boolean, boolean) with {@code permNegated} set to false.
     */
    IQueryRequest assignPermission(String permSID, int value, boolean permSkip);

    /**
     * @see #assignPermission(String, int, boolean) with {@code permSkip} set to false.
     */
    IQueryRequest assignPermission(String permSID, int value);

    /**
     * Returns a query request that will tell the TeamSpeak server to remove the permission from this subject.
     */
    IQueryRequest unassignPermission(String permSID);

    /**
     * Specialized getter for TS3 server groups.
     */
    List<ITS3Group> getServerGroups();

    /**
     * Specialized getter for the TS3 channel group.
     */
    ITS3Group getChannelGroup();
}
