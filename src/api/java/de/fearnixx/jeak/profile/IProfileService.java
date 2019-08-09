package de.fearnixx.jeak.profile;

import java.util.Optional;
import java.util.UUID;

/**
 * The profile services controls and provides access to {@link IUserProfile}s.
 */
public interface IProfileService {

    /**
     * @return An {@link Optional<IUserProfile>} when the profile exists. {@link Optional#empty()} otherwise.
     */
    Optional<IUserProfile> getProfile(UUID uuid);

    /**
     * Looks up the {@link UUID} for a given identity and returns the result from {@link #getProfile(UUID)}.
     * @return An {@link Optional<IUserProfile>} when a matching profile has been found and exists.
     *         {@link Optional#empty()} otherwise.
     */
    Optional<IUserProfile> getProfile(String ts3Identity);

    /**
     * Looks up the {@link UUID} for a given identity and returns a profile associated with that identity.
     * If no matching profile could be found, the profile will be created.
     * @return An {@link Optional#empty()} ONLY when the profile could not be created due to an error.
     *            {@link Optional<IUserProfile>} in the normal use case.
     * @apiNote Plugins are recommended to use {@link #getProfile(UUID)} or
     * {@link #getProfile(String)} whenever possible.
     *          For read-only access, this prevents the unnecessary creation of empty profiles.
     */
    Optional<IUserProfile> getOrCreateProfile(String ts3Identity);

    /**
     * Merges two profiles into one.
     * This is used to merge two profiles (with two identities) for one user.
     * The profile that is merged into, will be modified:
     * <ul>
     *     <li>Identities will be joined.</li>
     *     <li>Properties will be overwritten when they are set in {@code other}.</li>
     * </ul>
     * @implNote Invokes {@link #deleteProfile(UUID)} for {@code other}
     */
    void mergeProfiles(UUID into, UUID other);

    /**
     * Deletes a profile from the storage.
     */
    void deleteProfile(UUID uuid);
}
