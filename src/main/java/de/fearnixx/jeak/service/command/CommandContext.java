package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the rudimentary command context that is currently available.
 */
public class CommandContext extends BasicDataHolder implements ICommandContext {

    private TargetType type;
    private String command;
    private IQueryEvent.INotification.ITextMessage rawEvent;
    private final List<String> arguments;

    public CommandContext() {
        arguments = new ArrayList<>();
    }

    public List<String> getArguments() {
        return arguments;
    }

    public TargetType getTargetType() {
        return type;
    }

    public void setTargetType(TargetType type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public IQueryEvent.INotification.ITextMessage getRawEvent() {
        return rawEvent;
    }

    public void setRawEvent(IQueryEvent.INotification.ITextMessage rawEvent) {
        this.rawEvent = rawEvent;
    }
}
