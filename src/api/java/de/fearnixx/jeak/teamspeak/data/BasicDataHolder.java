package de.fearnixx.jeak.teamspeak.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation of the {@link IDataHolder} interface.
 */
public class BasicDataHolder implements IDataHolder {

    private final Object LOCK = new Object();
    private Map<String, String> values;

    public BasicDataHolder() {
        values = new ConcurrentHashMap<>();
    }

    public Map<String, String> getValues() {
        synchronized (LOCK) {
            return values;
        }
    }

    @Override
    public boolean hasProperty(String key) {
        return getProperty(key).isPresent();
    }

    @Override
    public Optional<String> getProperty(String key) {
        synchronized (LOCK) {
            return Optional.ofNullable(values.getOrDefault(key, null));
        }
    }

    @Override
    public void setProperty(String key, String value) {
        synchronized (LOCK) {
            if (value == null)
                values.remove(key);
            else
                values.put(key, value);
        }
    }

    @Override
    public void setProperty(String key, Object value) {
        setProperty(key, value != null ? value.toString() : null);
    }

    public IDataHolder copyFrom(IDataHolder other) {
        synchronized (LOCK) {
            this.values = new ConcurrentHashMap<>();
            return merge(other);
        }
    }

    public IDataHolder merge(IDataHolder other) {
        synchronized (LOCK) {
            for (Map.Entry<String, String> entry : other.getValues().entrySet()) {
                this.values.put(entry.getKey(), entry.getValue());
            }
        }

        return this;
    }
}
