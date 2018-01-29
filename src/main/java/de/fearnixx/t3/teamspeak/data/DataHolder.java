package de.fearnixx.t3.teamspeak.data;

import de.fearnixx.t3.event.QueryEvent;

import java.util.*;

/**
 * Created by MarkL4YG on 29-Jan-18
 */
public class DataHolder implements IDataHolder {

    private Map<String, String> values;

    public DataHolder(Map<String, String> values) {
        this.values = values;
    }

    public DataHolder() {
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
