package de.fearnixx.t3.service.perms;

import de.fearnixx.t3.service.perms.permission.IPermission;
import de.fearnixx.t3.service.perms.permission.IPermissionEntry;
import de.fearnixx.t3.ts3.keys.TargetType;

import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public interface IPermissionService {

    Optional<IPermission> getPermission(String qualifiedSID);

    Optional<IPermissionEntry> getEffectivePermission(String qualifiedSID, Integer target, TargetType type);

    List<IPermissionEntry> getPermissionContext(String qualifiedSID, Integer target, TargetType type);
}
