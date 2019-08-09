package de.fearnixx.jeak.service.command;

/**
 * Created by MarkL4YG on 15-Feb-18.
 */
public class CommandParserException extends CommandException {

    public CommandParserException(String msg) {
        super(msg);
    }

    public CommandParserException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
