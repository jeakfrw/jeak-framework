package de.fearnixx.jeak.teamspeak;

public enum KickType {

    FROM_SERVER(4),
    FROM_CHANNEL(5);

    private final int queryNumber;

    KickType(int queryNumber) {
        this.queryNumber = queryNumber;
    }

    public Integer getQueryNum() {
        return queryNumber;
    }
}
