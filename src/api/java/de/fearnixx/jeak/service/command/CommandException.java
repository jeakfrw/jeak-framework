package de.fearnixx.jeak.service.command;

/**
 * Generic exception to be thrown by {@link ICommandReceiver}s
 *   to indicate an issue that has to be reported to the user.
 */
public class CommandException extends Exception {

    public CommandException(String msg) {
        super(msg);
    }

    public CommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
