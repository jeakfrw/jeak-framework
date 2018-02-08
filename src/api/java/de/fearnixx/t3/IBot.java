package de.fearnixx.t3;

import de.fearnixx.t3.teamspeak.IServer;
import de.fearnixx.t3.teamspeak.cache.IDataCache;

import java.io.File;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IBot {

    File getBaseDirectory();

    IServer getServer();

    IDataCache getDataCache();

    void shutdown();
}
