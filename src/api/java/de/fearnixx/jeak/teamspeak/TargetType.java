package de.fearnixx.jeak.teamspeak;

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

    public static TargetType fromQueryNum(Integer qNum) throws IllegalArgumentException {
        for (TargetType tt : values()) {
            if (tt.getQueryNum().equals(qNum))
                return tt;
        }
        throw new IllegalArgumentException("Unknown query number: " + qNum);
    }
}
