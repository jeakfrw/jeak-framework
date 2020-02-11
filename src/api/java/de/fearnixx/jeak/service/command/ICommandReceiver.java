package de.fearnixx.jeak.service.command;

/**
 * Base interface for all commands that can be sent to the framework via. TS3 chat commands.
 *
 * @deprecated A rework of this is planned for an upcoming minor (1.X) release.
 * {@link Deprecated#forRemoval()} will be set once the replacement has been merged.
 * As 1.X.X is an LTS release, this interface will continue to work until 2.0.0.
 * If your command class declares a method "{@code public static String supersededBy()}", which returns the command to use instead,
 * the command service will notify users about the alternative.
 */
@Deprecated
public interface ICommandReceiver {

    /**
     * Receive a clients TextMessage. Can come from the following chat types:
     * * Private text
     * * Channel text
     * * Server text
     * <p>
     * Commands must start with "!" in order to be received.
     * CommandReceivers only receive the commands they are registered for.
     *
     * @param ctx The context of the executed command
     */
    void receive(ICommandContext ctx) throws CommandException;
}
