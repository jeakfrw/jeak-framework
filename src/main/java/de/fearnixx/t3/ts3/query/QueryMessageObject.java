package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.query.IQueryMessageObject;

import java.util.*;

/**
 * Created by Life4YourGames on 31.05.17.
 */
public class QueryMessageObject implements IQueryMessageObject {

    protected final Map<String, String> properties;
    protected final Object lock = new Object();

    public QueryMessageObject() {
        properties = new HashMap<>();
    }

    public void copyFrom(IQueryMessageObject obj) {
        synchronized (lock) {
            obj.getKeys().forEach(k -> {
                properties.put(k, obj.getProperty(k).get());
            });
        }
    }

    @Override
    public boolean hasProperty(String key) {
        synchronized (lock) {
            return properties.containsKey(key);
        }
    }

    @Override
    public Optional<String> getProperty(String key) {
        synchronized (lock) {
            return Optional.ofNullable(properties.getOrDefault(key, null));
        }
    }

    @Override
    public void setProperty(String key, String value) {
        synchronized (lock) {
            properties.put(key, value);
        }
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public class Error implements IQueryMessageObject.IError {

        private int id;
        private String message;

        public Error() {
            this(Integer.parseInt(getProperty("id").orElse("-1")), getProperty("msg").orElse("NO_MSG"));
        }

        protected Error(int id, String message) {
            this.id = id;
            this.message = message;
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public static Error ERROR_OK = new QueryMessageObject().new Error(0, "OK");
}
