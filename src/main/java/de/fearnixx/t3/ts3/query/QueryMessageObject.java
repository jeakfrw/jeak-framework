package de.fearnixx.t3.ts3.query;

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

    public static class Error extends QueryMessageObject implements IQueryMessageObject.IError {

        public static Error OK = new Error(0, "");

        private int id;
        private String message;

        public Error() {
            super();
        }

        private Error(int id, String msg) {
            this();
            this.id = 0;
            this.message = msg;
        }

        protected void read() {
            this.id = Integer.parseInt(getProperty("id").orElse("-1"));
            this.message = getProperty("msg").orElse("NO_MSG");
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "error{" + id + ", " + message + '}';
        }
    }
}
