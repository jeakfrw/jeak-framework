package de.fearnixx.jeak.service.command.adv;

import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Optional;

public interface IAdvClientCommandContext extends IAdvCommandContext {

    /**
     * Returns the client (sender) reference from the cache.
     */
    IClient getClientInvoker();
}
