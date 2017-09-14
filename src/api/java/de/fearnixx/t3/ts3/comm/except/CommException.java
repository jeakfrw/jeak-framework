package de.fearnixx.t3.service.comm.except;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public class CommException extends Exception {

    public CommException(String message) {
        super(message);
    }

    public CommException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class Closed extends CommException{

        public enum CloseReason {
            INTERNAL,
            CLIENT_DISCONNECTED,
            CLIENT_CLOSED,
            CONNECTION_LOST
        }

        private CloseReason reason;

        public Closed(CloseReason reason) {
            super("The channel has been closed");
            this.reason = reason;
        }

        public CloseReason getReason() {
            return reason;
        }
    }
}
