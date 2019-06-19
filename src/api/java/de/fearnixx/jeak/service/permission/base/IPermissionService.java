package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.service.permission.teamspeak.ITS3PermissionProvider;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Subject;

import java.util.Optional;
import java.util.UUID;

/**
 * General entry point for permission checks in the framework.
 * This can be used for plugins that require more specialized checks where {@link ISubject}, {@link IGroup} and {@link ITS3Subject} are not sufficient.
 */
public interface IPermissionService {

    /**
     * Optionally returns a permission provider for the specified system id.
     * At the moment, the only one automatically registered is {@link de.fearnixx.jeak.profile.IUserIdentity#SERVICE_TEAMSPEAK}.
     */
    Optional<IPermissionProvider> provide(String systemID);

    /**
     * Registers a new provider for other possible systems.
     * This allows external services to be connected into the framework and connector plugins to perform permission checks against them.
     */
    void registerProvider(String systemID, IPermissionProvider provider);

    /**
     * Shorthand-getter for the most common case: TS3 permission checks.
     * @implNote exactly the same as using {@link #provide(String)} with {@link de.fearnixx.jeak.profile.IUserIdentity#SERVICE_TEAMSPEAK}.
     */
    ITS3PermissionProvider getTS3Provider();

    /**
     * Shorthand-getter for the common case: Framework & Plugin permission checks.
     * @implNote exactly the same as using {@link #provide(String)} with {@code "jeak"}.
     */
    IPermissionProvider getFrameworkProvider();
}
