package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.query.QueryMessageObject;

/**
 * Created by MarkL4YG on 20.06.17.
 */
public class TS3Client extends QueryMessageObject implements IClient {

    boolean invalidated = false;

    public TS3Client(){
        super();
    }

    public void copyFrom(IQueryMessageObject obj) {
        synchronized (super.lock) {
            obj.getKeys().forEach(k -> {
                super.properties.put(k, obj.getProperty(k).get());
            });
        }
    }

    public void invalidate() {
        synchronized (super.lock) {
            invalidated = true;
        }
    }

    @Override
    public int getClientDBID() {
        return 0;
    }

    @Override
    public int getClientID() {
        return 0;
    }

    @Override
    public String getNickName() {
        return null;
    }
}
