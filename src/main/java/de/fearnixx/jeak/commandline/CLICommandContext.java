package de.fearnixx.jeak.commandline;

import de.fearnixx.jeak.service.command.CommandInfo;

import java.util.function.Consumer;

public class CLICommandContext {

    private final CommandInfo commInfo;
    private final Consumer<String> messageConsumer;

    public CLICommandContext(CommandInfo commInfo, Consumer<String> messageConsumer) {
        this.commInfo = commInfo;
        this.messageConsumer = messageConsumer;
    }

    public CommandInfo getCommandInfo() {
        return commInfo;
    }

    public Consumer<String> getMessageConsumer() {
        return messageConsumer;
    }
}
