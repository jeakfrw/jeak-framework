package de.fearnixx.t3.event.query;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface QNEServer extends IQueryEvent.INotification.Server {

    public static interface ClientEnter extends QNEServer {

    }

    public static interface ClientLeave extends QNEServer {

    }
}
