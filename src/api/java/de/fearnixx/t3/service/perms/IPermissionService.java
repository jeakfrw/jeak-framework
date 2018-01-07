package de.fearnixx.t3.service.perms;

import de.fearnixx.t3.service.perms.permission.IPermission;
import de.fearnixx.t3.service.perms.permission.IPermissionEntry;
import de.fearnixx.t3.service.perms.permission.PermSourceType;
import de.fearnixx.t3.ts3.keys.TargetType;

import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public interface IPermissionService {

    Optional<IPermission> getPermission(String qualifiedSID);

    Optional<IPermissionEntry> getEffectivePermission(String qualifiedSID, Integer target, PermSourceType type, Integer channelID);

    List<IPermissionEntry> getEffectivePermissionContext(String qualifiedSID, Integer target, PermSourceType type, Integer channelID);

    Optional<IPermProvider> getPermissionProvider(String providerID);

    void registerProvider(String providerID, IPermProvider provider);
}
