package de.fearnixx.t3.service.command;

import de.fearnixx.t3.event.IQueryEvent;
import de.fearnixx.t3.teamspeak.TargetType;

import java.util.List;

/**
 * Created by MarkL4YG on 15-Feb-18
 */
public interface ICommandContext {

    TargetType getTargetType();

    String getCommand();

    List<String> getArguments();

    IQueryEvent.INotification.ITextMessage getRawEvent();
}
