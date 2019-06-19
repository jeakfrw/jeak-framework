package de.fearnixx.jeak.service.permission.teamspeak;

import java.util.Optional;
import java.util.UUID;

/**
 * Specialized permission provider that is capable of reflecting the permission evaluation from TeamSpeak 3.
 */
public interface ITS3PermissionProvider {

    Optional<ITS3Permission> getActivePermission(String clientUniqueID, String permSID);

    Optional<ITS3Permission> getActivePermission(Integer clientID, String permSID);

    Optional<ITS3Permission> getClientPermission(Integer clientDBID, String permSID);

    Optional<ITS3Permission> getServerGroupPermission(Integer serverGroupID, String permSID);

    Optional<ITS3Permission> getChannelGroupPermission(Integer channelGroupID, String permSID);

    Optional<ITS3Permission> getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID);

    Optional<ITS3Permission> getChannelPermission(Integer channelID, String permSID);

    /**
     * Translates a given string permission representation into the corresponding numeric ID.
     */
    Integer translateSID(String permSID);

    /**
     * Use sparingly!
     * When the TeamSpeak 3 server is not using a connectable database or the persistence unit is not configured for the framework,
     * the service will cache permlist-responses from the server to reduce response times.
     * If cache invalidation is not sufficient, this can be used to wipe cached entries.
     */
    void clearCache(ITS3Permission.PriorityType type, Integer optClientOrGroupID, Integer optChannelID);
}
