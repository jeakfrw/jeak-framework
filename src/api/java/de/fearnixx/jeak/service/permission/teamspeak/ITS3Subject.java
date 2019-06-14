package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.service.permission.base.ISubject;

import java.util.Optional;

/**
 * Special subject class for subjects that are also present on TeamSpeak 3.
 */
public interface ITS3Subject extends ISubject {

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
}
