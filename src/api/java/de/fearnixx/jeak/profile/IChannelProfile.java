package de.fearnixx.jeak.profile;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for persisted custom information about channels.
 * Channel profiles are directly associated with their channel ID. That means, when the channel is deleted (or detected as deleted), the associated profile will also be removed.
 */
public interface IChannelProfile {

    /**
     * Persistently sets an option for this channel.
     */
    void setOption(String optionId, String value);

    /**
     * Optionally, returns the stored value for a given option id.
     */
    Optional<String> getOption(String optionId);

    /**
     * Short-hand for {@link #getOption(String)}s {@code #orElse} function.
     * Returns {@code def} when the option is not present.
     */
    String getOption(String optionId, String def);

    /**
     * Returns whether or not the given option id is present on this profile.
     */
    boolean hasOption(String optionId);

    /**
     * Returns all options stored in this profile.
     */
    Map<String, String> getOptions();
}
