package de.fearnixx.t3;

import de.fearnixx.t3.service.IServiceManager;
import de.fearnixx.t3.service.db.IDBReader;
import de.fearnixx.t3.ts3.ITS3Server;
import de.fearnixx.t3.task.ITaskManager;
import de.fearnixx.t3.event.IEventManager;
import de.fearnixx.t3.ts3.command.ICommandManager;

import java.io.File;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public interface IT3Bot {

    /**
     * Due to the intended multi-instance support plugins are advised to work with the FS under this directory
     * instead of the processes current directory
     * @return The working directory assigned to this bot instance
     */
    File getDir();

    /**
     * @return The configuration directory assigned to this bot instance
     */
    File getConfDir();

    /**
     * @return The logs directory assigned to this bot instance
     */
    File getLogDir();

    /**
     * Abstract representation of the TS3 server this bot is connected with
     * @return The server representation
     */
    ITS3Server getServer();

    /**
     * API support for plugins - Allows registering own or retrieving services
     * @return The service manager
     */
    IServiceManager getServiceManager();

    /**
     * Allows plugins to fire events originating from this bot
     * @return The event manager
     */
    IEventManager getEventManager();

    /**
     * Allows plugins to schedule asynchronous tasks based on a delay or interval
     * @return The task manager
     */
    ITaskManager getTaskManager();

    /**
     * @return The CommandManager
     */
    ICommandManager getCommandManager();

    /**
     * A way to access values stored in the TS3DB
     * @return The DB reader
     */
    IDBReader getDBReader();

    /**
     * Shut down this bot
     */
    void shutdown();
}
