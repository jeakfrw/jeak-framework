package de.fearnixx.jeak.profile;

import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User profiles offer a way to store data independently from {@link IClient} which
 * only is available while the user is online.
 */
public interface IUserProfile {

    /**
     * A unique ID created when the profile is registered for the first time.
     */
    UUID getUniqueId();

    /**
     * List of TeamSpeak identities linked to this profile.
     * As provided by {@link IClient#getClientUniqueID()}
     */
    List<String> getTSIdentities();

    /**
     * Returns the value stored for an option.
     * @return {@link Optional<String>} if the value is stored. {@link Optional#empty()} otherwise.
     */
    Optional<String> getOption(String optionId);

    /**
     * Convenience method for use in place of {@link #getOption(String)}.
     * If the Optional would be empty, the provided default is returned.
     */
    String getOption(String optionId, String def);

    /**
     * Sets / Adds an option for this profile.
     * @implNote if the provided value is {@code null}, {@link #removeOption(String)} is invoked.
     */
    void setOption(String optionId, String value);

    /**
     * Removes an option from this profile.
     */
    void removeOption(String optionId);
}
