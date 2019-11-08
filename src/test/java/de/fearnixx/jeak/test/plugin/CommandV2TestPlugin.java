package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import static de.fearnixx.jeak.service.command.spec.Commands.*;

@JeakBotPlugin(id = "commandv2test")
public class CommandV2TestPlugin extends AbstractTestPlugin {

    @Inject
    private ICommandService commandService;

    @Inject
    private IServer server;

    public CommandV2TestPlugin() {
        addTest("outer_executed");
        addTest("inner_executed");
    }

    @Listener
    public void onInit(IBotStateEvent.IInitializeEvent event) {
        ICommandSpec spec = commandSpec("test-command", "tc")
                .arguments(
                        argumentSpec("arg", "a", String.class),
                        argumentSpec("barg", "b", Integer.class)
                )
                .permission("test.command")
                .executor(ctx -> {
                    this.success("outer_executed");
                    server.getConnection().sendRequest(ctx.getSender().sendMessage("Your o-input: " + ctx.getOne("barg")));
                })
                .build();
        commandService.registerCommand(spec);

        ICommandSpec spec2 = commandSpec("test")
                .parameters(
                        paramSpec("something", String.class),
                        paramSpec().optional(paramSpec("someElse", IUser.class))
                )
                .permission("test.subcommand", 4)
                .executor(ctx -> {
                    this.success("inner_executed");
                    StringBuilder message = new StringBuilder("Your input: " + ctx.getOne("something"));

                    ctx.getOne("someElse", IUser.class)
                            .ifPresent(u -> {
                                message.append(" Second parameter: ").append(u.toString());
                            });

                    server.getConnection().sendRequest(ctx.getSender().sendMessage(message.toString()));
                })
                .build();
        commandService.registerCommand(spec2);
    }
}
