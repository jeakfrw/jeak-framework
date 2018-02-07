package de.fearnixx.t3.service.permission.teamspeak;

import java.util.Optional;

/**
 * Created by MarkL4YG on 04-Feb-18
 */
public interface ITS3PermissionService {

    Optional<ITS3Permission> getActivePermission(Integer clientID, String permSID);
}
