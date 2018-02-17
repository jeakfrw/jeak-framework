package de.fearnixx.t3.service.command;

/**
 * Created by MarkL4YG on 17-Feb-18
 */
public class CommandException extends Exception {

    public CommandException(String msg) {
        super(msg);
    }

    public CommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
