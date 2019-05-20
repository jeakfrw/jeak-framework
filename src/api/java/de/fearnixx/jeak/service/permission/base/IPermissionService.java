package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.service.permission.teamspeak.ITS3PermissionProvider;

import java.util.Optional;

/**
 *
 * @auth MarkL4YG
 */
public interface IPermissionService {

    /**
     * Optionally returns a permission provider for the specified system id.
     * At the moment, the only one automatically registered is {@link de.fearnixx.jeak.profile.IUserIdentity#SERVICE_TEAMSPEAK}.
     */
    Optional<IPermissionProvider> provide(String systemID);

    void registerProvider(String systemID, IPermissionProvider provider);

    /**
     * Shorthand-getter for the most common case: TS3 permission checks.
     * @implNote exactly the same as using {@link #provide(String)} with {@link de.fearnixx.jeak.profile.IUserIdentity#SERVICE_TEAMSPEAK}.
     */
    ITS3PermissionProvider getTS3Provider();
}
