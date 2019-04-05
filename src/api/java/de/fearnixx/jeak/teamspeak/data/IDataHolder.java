package de.fearnixx.jeak.teamspeak.data;

import java.util.Map;
import java.util.Optional;

/**
 * Generic data holder.
 * Basically a wrapper around a map with some convenience methods.
 *
 * @implNote {@link BasicDataHolder}s use concurrent maps for thread safety.
 */
public interface IDataHolder {

    /**
     * Whether or not the given key is currently stored in this holder.
     */
    boolean hasProperty(String key);

    /**
     * Optionally returns the stored value for this key.
     */
    Optional<String> getProperty(String key);

    /**
     * Sets or replaces a stored value.
     * A value of {@code null} will remove the key from the holder.
     */
    void setProperty(String key, String value);

    /**
     * Convenience overload for {@link #setProperty(String, String)}.
     * Values will be {@link Object#toString()}ed automatically.
     */
    void setProperty(String key, Object value);

    Map<String, String> getValues();

    /**
     * Reset all stored values and initialize from another holder.
     */
    IDataHolder copyFrom(IDataHolder other);

    /**
     * Merge all values from another holder.
     * Values present on both holders will override the ones present in {@code this} holder.
     */
    IDataHolder merge(IDataHolder other);
}
