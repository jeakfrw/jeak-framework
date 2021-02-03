package de.fearnixx.jeak.teamspeak.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Basic implementation of the {@link IDataHolder} interface.
 */
public class BasicDataHolder implements IDataHolder {

    private Map<String, String> values;

    public BasicDataHolder() {
        values = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    public synchronized Map<String, String> getValues() {
        return values;
    }

    @Override
    public boolean hasProperty(String key) {
        return getProperty(key).isPresent();
    }

    @Override
    public synchronized Optional<String> getProperty(String key) {
        return Optional.ofNullable(values.getOrDefault(key, null));
    }

    @Override
    public synchronized void setProperty(String key, String value) {
        if (value == null)
            values.remove(key);
        else
            values.put(key, value);
    }

    @Override
    public void setProperty(String key, Object value) {
        setProperty(key, value != null ? value.toString() : null);
    }

    public synchronized IDataHolder copyFrom(IDataHolder other) {
        this.values = new ConcurrentHashMap<>();
        return merge(other);
    }

    public synchronized IDataHolder merge(IDataHolder other) {
        for (Map.Entry<String, String> entry : other.getValues().entrySet()) {
            this.values.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public String toString() {
        final var mapBody = getValues().entrySet()
                .stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));
        return String.format("BasicDataHolder{values=%s}", mapBody);
    }
}
