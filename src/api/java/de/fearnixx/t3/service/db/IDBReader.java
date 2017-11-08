package de.fearnixx.t3.service.db;

import de.fearnixx.t3.ts3.client.IDBClient;

import java.util.Optional;

/**
 * The DB reader service allows you to get information from the TS3-DB.
 *
 * The primary source however is still the server query connection as this service mainly uses the db commands.
 *
 * Created by MarkL4YG on 30.06.17.
 */
public interface IDBReader {

    /**
     * @param cldbid The clients database ID
     * @return The clients database information
     */
    Optional<IDBClient> getClientDBInfo(int cldbid);
}
