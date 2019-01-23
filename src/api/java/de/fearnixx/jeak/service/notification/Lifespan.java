package de.fearnixx.jeak.service.notification;

public enum Lifespan {

    /**
     * Short-term lifetime.
     * "Valid" for a few minutes to an hour.
     */
    SHORT(100),

    /**
     * Shorter than basic lifetime.
     * "Valid" for up to a day.
     */
    SHORTER(500),

    /**
     * Basic lifetime.
     * "Valid" for about one or two days.
     */
    BASIC(1000),

    /**
     * Longer than basic lifetime.
     * "Valid" for about one or two weeks.
     */
    LONGER(5000),

    /**
     * Long-term lifetime.
     * "Valid" longer than two weeks.
     */
    LONG(10000),


    /**
     * Longest-term lifetime.
     * "Valid" forever.
     */
    FOREVER(Integer.MAX_VALUE);

    private int level;

    Lifespan(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
