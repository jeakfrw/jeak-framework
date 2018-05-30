package de.fearnixx.t3.service.command;

import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.teamspeak.TargetType;

import java.util.List;

/**
 * Wrapper class for commands to offer some convenience methods.
 */
public interface ICommandContext {

    TargetType getTargetType();

    /**
     * The command that's been sent.
     */
    String getCommand();

    /**
     * Basic list of arguments parsed by a rudimentary parser.
     * Separates by spaces.
     */
    List<String> getArguments();

    IQueryEvent.INotification.ITextMessage getRawEvent();
}
