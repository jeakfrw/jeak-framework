package de.fearnixx.t3.service.command;

import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.teamspeak.TargetType;
import de.fearnixx.t3.teamspeak.data.DataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarkL4YG on 15-Feb-18
 */
public class CommandContext extends DataHolder implements ICommandContext {

    private TargetType type;
    private String command;
    private List<String> arguments;
    private IQueryEvent.INotification.ITextMessage rawEvent;

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
