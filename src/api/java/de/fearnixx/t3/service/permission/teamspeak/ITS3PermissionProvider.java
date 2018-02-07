package de.fearnixx.t3.service.permission.teamspeak;

import de.fearnixx.t3.service.permission.base.IPermissionProvider;

import java.util.Optional;

/**
 * Created by MarkL4YG on 04-Feb-18
 */
public interface ITS3PermissionProvider extends IPermissionProvider {

    Optional<ITS3Permission> getActivePermission(Integer clientID, String permSID);
}
