package de.fearnixx.jeak.teamspeak.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation of the {@link IDataHolder} interface.
 */
public class BasicDataHolder implements IDataHolder {

    private final Object lock = new Object();
    private Map<String, String> values;

    public BasicDataHolder() {
        values = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getValues() {
        synchronized (lock) {
            return values;
        }
    }

    @Override
    public boolean hasProperty(String key) {
        return getProperty(key).isPresent();
    }

    @Override
    public Optional<String> getProperty(String key) {
        synchronized (lock) {
            return Optional.ofNullable(values.getOrDefault(key, null));
        }
    }

    @Override
    public void setProperty(String key, String value) {
        synchronized (lock) {
            if (value == null) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        setProperty(key, value != null ? value.toString() : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDataHolder copyFrom(IDataHolder other) {
        synchronized (lock) {
            this.values = new ConcurrentHashMap<>();
            return merge(other);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDataHolder merge(IDataHolder other) {
        synchronized (lock) {
            for (Map.Entry<String, String> entry : other.getValues().entrySet()) {
                this.values.put(entry.getKey(), entry.getValue());
            }
        }

        return this;
    }
}
