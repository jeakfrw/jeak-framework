package de.fearnixx.jeak.service.notification;

/**
 *
 */
public enum Urgency {

    DISMISSABLE(100),
    BASIC(1000),
    WARN(500000),
    ALERT(1000000);

    private final int level;

    Urgency(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
