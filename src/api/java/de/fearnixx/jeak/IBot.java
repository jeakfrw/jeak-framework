package de.fearnixx.jeak;

import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;

import java.io.File;
import java.util.UUID;

/**
 * "Main" class of the current instance of the framework.
 */
public interface IBot {

    /**
     * Instances all have a base-directory that may be different from the process working directory.
     * Use this to make sure, you're writing files to the correct location.
     */
    File getBaseDirectory();

    /**
     * The configuration directory may be different from the base directory.
     * Use this if you require manual navigation through config files.
     */
    File getConfigDirectory();

    /**
     * Returns the representation of the server, the main connection is currently connected to.
     */
    IServer getServer();

    /**
     * Returns the cache currently associated with the server.
     * @deprecated Will be entirely moved to {@link IServer} as it conveys the direct relation better.
     */
    @Deprecated
    IDataCache getDataCache();

    /**
     * Request the framework to shut down.
     * Will attempt to close connections and call shut down events.
     */
    void shutdown();

    /**
     * Different instances of the framework are additionally identified with {@link UUID}s even within the same JVM.
     */
    UUID getInstanceUUID();
}
