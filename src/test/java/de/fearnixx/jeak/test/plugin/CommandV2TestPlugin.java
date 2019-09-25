package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import static de.fearnixx.jeak.service.command.spec.Commands.*;

@JeakBotPlugin(id = "commandv2test")
public class CommandV2TestPlugin extends AbstractTestPlugin {

    @Listener
    public void onInit(IBotStateEvent.IInitializeEvent event) {
        ICommandSpec spec = commandSpec("test-command", "tc")
                .arguments(
                        argumentSpec("arg", "a", String.class),
                        argumentSpec("barg", "b", Integer.class)
                )
                .subcommand(
                        commandSpec("test")
                                .parameters(
                                        paramSpec("something", String.class),
                                        paramSpec().optional(paramSpec("someElse", String.class))
                                )
                                .build()
                )
                .build();
    }
}
