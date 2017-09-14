package de.fearnixx.t3.ts3.query;

import java.util.Optional;
import java.util.Set;

/**
 * Created by MarkL4YG on 10.06.17.
 */
public interface IQueryMessageObject {

    boolean hasProperty(String key);
    Optional<String> getProperty(String key);
    void setProperty(String key, String value);

    Set<String> getKeys();

    interface IError extends IQueryMessageObject {

        int getID();
        String getMessage();
    }
}
