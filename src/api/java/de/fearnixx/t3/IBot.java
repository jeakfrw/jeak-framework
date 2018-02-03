package de.fearnixx.t3;

import de.fearnixx.t3.teamspeak.IServer;
import de.fearnixx.t3.teamspeak.cache.IDataCache;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IBot {

    IServer getServer();

    IDataCache getDataCache();

    void shutdown();
}
