package de.fearnixx.t3.ts3.keys;

/**
 * Created by Life4YourGames on 05.07.17.
 *
 * Enumerated representation of possible target types in the ServerQuery
 */
public enum TargetType {
    CLIENT(1),
    CHANNEL(2),
    SERVER(3);

    private final Integer query_value;

    TargetType(Integer query_value) {
        this.query_value = query_value;
    }

    public Integer getQueryNum() {
        return query_value;
    }

    public static TargetType valueOf(int i) {
        return values()[i-1];
    }
}
