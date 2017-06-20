package de.fearnixx.t3.query;

import java.util.List;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IQueryMessage {

    public static enum MsgType {
        RESPONSE,
        NOTIFYSERVER,
        NOTIFYCHANNEL,
        NOTIFYTEXTSERVER,
        NOTIFYTEXTCHANNEL,
        NOTIFYTEXTPRIVATE
    }

    /**
     * @return How many objects have been received
     */
    int getObjectCount();

    /**
     * @param o the index
     * @return The object at index 'o'
     * @throws IndexOutOfBoundsException when 'o' \< 0 or 'o' \> {@link #getObjectCount()} + 1
     * @implNote Due to implementation the object at index {@link #getObjectCount()} is the error-object
     */
    IQueryMessageObject getObject(int o);

    /**
     * @return An immutable list of all objects that've been received
     * @implNote This does NOT contain the error-object!
     */
    List<IQueryMessageObject> getObjects();

    MsgType getType();

    IQueryMessageObject.IError getError();
}
