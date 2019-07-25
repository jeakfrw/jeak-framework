package de.fearnixx.jeak.service.command.adv.spec;

import de.fearnixx.jeak.service.command.ICommandContext;
import de.fearnixx.jeak.service.command.adv.ICommandExecutor;

import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public interface ICommandSpec {

    String pluginId();

    String commandName();

    ICommandExecutor executor();

    Map<String, ICommandSpec> subCommands();
}
