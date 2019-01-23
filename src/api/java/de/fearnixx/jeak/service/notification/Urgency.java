package de.fearnixx.jeak.service.notification;

/**
 *
 */
public enum Urgency {

    DISMISSABLE(100),
    BASIC(1000),

    ALERT(1000000);

    private int level;

    Urgency(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
