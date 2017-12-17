package de.fearnixx.t3.service.perms;

import de.fearnixx.t3.service.perms.permission.PermSourceType;
import de.fearnixx.t3.ts3.client.IClient;
import de.fearnixx.t3.ts3.client.IDBClient;
import de.fearnixx.t3.ts3.keys.TargetType;
import de.fearnixx.t3.service.perms.permission.IPermission;
import de.fearnixx.t3.service.perms.permission.IPermissionEntry;

import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public interface IPermProvider {

    Optional<IPermission> getPermission(String systemSID);

    Optional<IPermissionEntry> getEffectivePermission(String systemSID, Integer target, PermSourceType type);

    List<IPermissionEntry> getPermissionContext(String systemSID, IDBClient client);

    Optional<IPermissionEntry> getClientPermission(String systemID, IDBClient client);
}
