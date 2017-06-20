package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.query.IQueryMessageObject;

/**
 * Created by MarkL4YG on 20.06.17.
 *
 * Abstract representation of online clients
 */
public interface IClient extends IQueryMessageObject {

    /**
     * @return The clients ID
     * @apiNote This is only valid while the client is online. Refer to {@link #getClientDBID()}
     */
    int getClientID();

    /**
     * @return The database ID of this client
     */
    int getClientDBID();

    /**
     * @return The clients nick name
     */
    String getNickName();
}
