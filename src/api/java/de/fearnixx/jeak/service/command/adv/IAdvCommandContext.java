package de.fearnixx.jeak.service.command.adv;

import java.util.Optional;

public interface IAdvCommandContext {

    /**
     * Whether or not the command was invoked from the console.
     * This will cause the associated client to be empty.
     */
    boolean isConsole();

    /**
     * Whether or not the command was invoked by a client.
     */
    boolean isClient();

    Object getParameter(String name);

    <T> Optional<T> getParameter(String name, Class<T> hint);
}
