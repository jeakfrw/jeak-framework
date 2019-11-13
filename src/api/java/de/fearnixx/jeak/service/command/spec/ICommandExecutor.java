package de.fearnixx.jeak.service.command.spec;

import de.fearnixx.jeak.service.command.CommandException;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;

public interface ICommandExecutor {

    void execute(ICommandExecutionContext exec) throws CommandException;
}
