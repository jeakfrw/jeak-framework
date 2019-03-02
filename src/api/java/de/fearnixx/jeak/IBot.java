package de.fearnixx.jeak;

import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;

import java.io.File;
import java.util.UUID;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IBot {

    File getBaseDirectory();

    IServer getServer();

    IDataCache getDataCache();

    void shutdown();

    UUID getInstanceUUID();
}
