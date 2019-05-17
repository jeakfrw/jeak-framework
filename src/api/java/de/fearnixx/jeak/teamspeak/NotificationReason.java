package de.fearnixx.jeak.teamspeak;

/**
 * Information taken from: https://yat.qa/ressourcen/server-query-notify/#notifycliententerview
 */
public enum NotificationReason {

    /**
     * Voluntarily invoked action.
     */
    SELF_INVOKED(0),

    /**
     * Forced move by other client or server.
     */
    MOVED(1),

    /**
     * Connection read timed out.
     */
    TIMEOUT(3),

    /**
     * Kicked from channel.
     */
    CHANNEL_KICK(4),

    /**
     * Kicked from server.
     */
    SERVER_KICK(5),

    /**
     * Banned from server.
     */
    BANNED(6),

    /**
     * Voluntarily left the server.
     */
    LEFT_SERVER(8),

    /**
     * Server or channel has been edited.
     */
    EDITED(10),

    /**
     * Server is shutting down.
     */
    SERVER_SHUTDOWN(11);

    private int reasonId;

    NotificationReason(int reasonId) {
        this.reasonId = reasonId;
    }

    public int getReasonId() {
        return reasonId;
    }

    public static NotificationReason forReasonId(Integer id) {
        if (id < 0) {
            throw new IllegalArgumentException("Invalid reasonId: " + id);
        }
        for (NotificationReason value : NotificationReason.values()) {
            if (value.getReasonId() == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown reasonId: " + id);
    }
}
