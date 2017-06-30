package de.fearnixx.t3.service.db;

import de.fearnixx.t3.ts3.client.IDBClient;

import java.util.Optional;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public interface IDBReader {

    /**
     * @param cldbid
     * @return
     */
    Optional<IDBClient> getClientDBInfo(int cldbid);
}
