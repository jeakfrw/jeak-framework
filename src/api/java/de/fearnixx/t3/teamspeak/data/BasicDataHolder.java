package de.fearnixx.t3.teamspeak.data;

import java.util.*;

/**
 * Basic implementation of the {@link IDataHolder} interface.
 */
public class BasicDataHolder implements IDataHolder {

    private Map<String, String> values;

    public BasicDataHolder(Map<String, String> values) {
        this.values = values;
    }

    public BasicDataHolder() {
        values = new HashMap<>();
    }

    public Map<String, String> getValues() {
        return values;
    }

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(values.getOrDefault(key, null));
    }

    @Override
    public void setProperty(String key, String value) {
        values.put(key, value);
    }

    public void copyFrom(IDataHolder other) {
        this.values = new HashMap<>();
        merge(other);
    }

    public void merge(IDataHolder other) {
        for (Map.Entry<String, String> entry : other.getValues().entrySet()) {
            this.values.put(entry.getKey(), entry.getValue());
        }
    }
}
