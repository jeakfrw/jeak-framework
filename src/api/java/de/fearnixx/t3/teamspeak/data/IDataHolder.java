package de.fearnixx.t3.teamspeak.data;

import java.util.Map;
import java.util.Optional;

/**
 * Generic data holder.
 * Basically a wrapper around a map with some convenience methods.
 */
public interface IDataHolder {

    boolean hasProperty(String key);

    Optional<String> getProperty(String key);

    void setProperty(String key, String value);

    Map<String, String> getValues();

    IDataHolder copyFrom(IDataHolder other);

    IDataHolder merge(IDataHolder other);
}
