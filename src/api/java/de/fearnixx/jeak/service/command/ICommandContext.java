package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.TargetType;

import java.util.List;

/**
 * Wrapper class for commands to offer some convenience methods.
 * @deprecated see {@link ICommandReceiver}.
 */
@Deprecated
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
