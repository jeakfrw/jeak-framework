package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.service.permission.base.IPermissionProvider;

import java.util.Optional;

/**
 * Created by MarkL4YG on 04-Feb-18
 */
public interface ITS3PermissionProvider extends IPermissionProvider {

    void clearCache(ITS3Permission.PriorityType type, Integer optClientOrGroupID, Integer optChannelID);

    Optional<ITS3Permission> getActivePermission(Integer clientID, String permSID);

    Optional<ITS3Permission> getClientPermission(Integer clientDBID, String permSID);

    Optional<ITS3Permission> getServerGroupPermission(Integer serverGroupID, String permSID);

    Optional<ITS3Permission> getChannelGroupPermission(Integer channelGroupID, String permSID);

    Optional<ITS3Permission> getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID);

    Optional<ITS3Permission> getChannelPermission(Integer channelID, String permSID);

    Integer translateSID(String permSID);
}
