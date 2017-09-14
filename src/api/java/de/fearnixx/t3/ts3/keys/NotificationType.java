package de.fearnixx.t3.ts3.keys;

/**
 * Created by Life4YourGames on 06.07.17.
 *
 * Enumerative representation for possible notifications
 */
public enum NotificationType {
    CLIENT_ENTER("server", 1),
    CLIENT_LEAVE("server", 1),

    TEXT_PRIVATE("textprivate", 2),
    TEXT_CHANNEL("textchannel", 4),
    TEXT_SERVER("textserver", 8);


    private final String queryID;
    private final int mod;
    NotificationType(String queryID, int mod) {
        this.queryID = queryID;
        this.mod = mod;
    }

    public String getQueryID() {
        return queryID;
    }

    public int getMod() {
        return mod;
    }
}
