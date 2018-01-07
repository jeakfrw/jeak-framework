package de.fearnixx.t3.service.perms;

import de.fearnixx.t3.service.perms.permission.IPermission;
import de.fearnixx.t3.service.perms.permission.IPermissionEntry;
import de.fearnixx.t3.service.perms.permission.PermSourceType;
import de.fearnixx.t3.ts3.keys.TargetType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public class PermissionService implements IPermissionService {

    private ConcurrentHashMap<String, IPermProvider> providers = new ConcurrentHashMap<>();

    @Override
    public Optional<IPermission> getPermission(String qualifiedSID) {
        Integer colIndex = qualifiedSID.indexOf(':');
        if (colIndex < 1) {
            throw new IllegalArgumentException("Invalid qualified permission ID: " + qualifiedSID);
        }
        String providerID = qualifiedSID.substring(0, colIndex);
        String permID = qualifiedSID.substring(colIndex+1);
        Optional<IPermProvider> provider = getPermissionProvider(providerID);
        if (!provider.isPresent()) {
            return Optional.empty();
        }
        return provider.get().getPermission(permID);
    }

    @Override
    public Optional<IPermissionEntry> getEffectivePermission(String qualifiedSID, Integer target, PermSourceType type, Integer channelID) {
        Integer colIndex = qualifiedSID.indexOf(':');
        if (colIndex < 1) {
            throw new IllegalArgumentException("Invalid qualified permission ID: " + qualifiedSID);
        }
        String providerID = qualifiedSID.substring(0, colIndex);
        String permID = qualifiedSID.substring(colIndex+1);
        Optional<IPermProvider> provider = getPermissionProvider(providerID);
        if (!provider.isPresent()) {
            return Optional.empty();
        }
        return provider.get().getEffectivePermission(permID, target, type, channelID);
    }

    @Override
    public List<IPermissionEntry> getEffectivePermissionContext(String qualifiedSID, Integer target, PermSourceType type, Integer channelID) {
        Integer colIndex = qualifiedSID.indexOf(':');
        if (colIndex < 1) {
            throw new IllegalArgumentException("Invalid qualified permission ID: " + qualifiedSID);
        }
        String providerID = qualifiedSID.substring(0, colIndex);
        String permID = qualifiedSID.substring(colIndex+1);
        Optional<IPermProvider> provider = getPermissionProvider(providerID);
        if (!provider.isPresent()) {
            return Collections.emptyList();
        }
        return provider.get().getEffectivePermissionContext(permID, target, type, channelID);
    }

    @Override
    public Optional<IPermProvider> getPermissionProvider(String providerID) {
        if (providerID == null || providerID.trim().isEmpty()) {
            throw new IllegalArgumentException(new NullPointerException("Null provider passed!"));
        }
        return Optional.ofNullable(providers.getOrDefault(providerID.trim(), null));
    }

    @Override
     public void registerProvider(String providerID, IPermProvider provider) {
        if (provider != null) {
            providers.put(providerID, provider);
        } else {
            providers.remove(providerID);
        }
     }
}
