package de.fearnixx.t3.teamspeak.data;

import java.util.Map;
import java.util.Optional;

/**
 * Created by MarkL4YG on 29-Jan-18
 */
public interface IDataHolder {

    Optional<String> getProperty(String key);

    void setProperty(String key, String value);

    Map<String, String> getValues();

    void copyFrom(IDataHolder other);

    void merge(IDataHolder other);
}
