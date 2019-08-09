package de.fearnixx.jeak.teamspeak;

/**
 * Created by Life4YourGames on 05.07.17.
 *
 * <p>Enumerated representation of possible target types in the ServerQuery
 */
public enum TargetType {
    CLIENT(1),
    CHANNEL(2),
    SERVER(3);

    private final Integer queryValue;

    TargetType(Integer queryValue) {
        this.queryValue = queryValue;
    }

    public Integer getQueryNum() {
        return queryValue;
    }

    public static TargetType valueOf(int i) {
        return values()[i - 1];
    }

    public static TargetType fromQueryNum(Integer queryNumber) {
        for (TargetType tt : values()) {
            if (tt.getQueryNum().equals(queryNumber)) {
                return tt;
            }
        }
        throw new IllegalArgumentException("Unknown query number: " + queryNumber);
    }
}
