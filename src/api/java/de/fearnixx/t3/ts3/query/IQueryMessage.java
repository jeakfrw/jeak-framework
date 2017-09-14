package de.fearnixx.t3.ts3.query;

import java.util.List;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IQueryMessage {

    /**
     * @return An immutable list of all objects that've been received
     * @implNote This does NOT contain the error-object!
     */
    List<IQueryMessageObject> getObjects();

    IQueryMessageObject.IError getError();
}
