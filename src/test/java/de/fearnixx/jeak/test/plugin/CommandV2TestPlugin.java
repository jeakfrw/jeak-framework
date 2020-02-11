package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import java.util.Optional;

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
        addTest("firstOf_executed");
        addTest("clashed_name");
        addTest("clashed_shorthand");
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

        ICommandSpec spec3 = commandSpec("first-of", "commandv2test:first-of")
                .parameters(
                        paramSpec().firstMatching(
                                paramSpec("client", IClient.class),
                                paramSpec("user", IUser.class),
                                paramSpec("channel", IChannel.class)
                        )
                )
                .permission("test.thirdcommand")
                .executor(ctx -> {
                    success("firstOf_executed");
                    Optional<IClient> client = ctx.getOne("client", IClient.class);
                    Optional<IUser> user = ctx.getOne("user", IUser.class);
                    Optional<IChannel> channel = ctx.getOne("channel", IChannel.class);
                    client.ifPresent(c ->
                            ctx.getConnection().sendRequest(ctx.getSender().sendMessage("Client: " + c.toString()))
                    );
                    user.ifPresent(u ->
                            ctx.getConnection().sendRequest(ctx.getSender().sendMessage("User: " + u.toString()))
                    );
                    channel.ifPresent(c ->
                            ctx.getConnection().sendRequest(ctx.getSender().sendMessage("Channel: " + c.toString()))
                    );
                })
                .build();
        commandService.registerCommand(spec3);

        ICommandSpec spec4 = commandSpec("argumentized", "commandv2test:argumentized")
                .arguments(
                        argumentSpec("name", "n", IClient.class),
                        argumentSpec().optional(argumentSpec("channel", "c", IChannel.class))
                )
                .executor(ctx -> {
                    Optional<IClient> optClient = ctx.getOne("name", IClient.class);
                    optClient.ifPresent(c ->
                            ctx.getConnection().sendRequest(ctx.getSender().sendMessage("Client: " + c.toString()))
                    );
                    Optional<IChannel> optChannel = ctx.getOne("channel", IChannel.class);
                    optChannel.ifPresent(c ->
                            ctx.getConnection().sendRequest(ctx.getSender().sendMessage("Channel: " + c.toString()))
                    );
                })
                .build();
        commandService.registerCommand(spec4);

        ICommandSpec spec5 = commandSpec("clashed")
                .arguments(
                        argumentSpec("name", "n", IUser.class),
                        argumentSpec("name", "s", IChannel.class)
                )
                .build();
        try {
            commandService.registerCommand(spec5);
        } catch (IllegalArgumentException e) {
            success("clashed_name");
        }

        ICommandSpec spec6 = commandSpec("clashed2")
                .arguments(
                        argumentSpec("name", "n", IUser.class),
                        argumentSpec("neighbor", "n", IChannel.class)
                )
                .executor(ctx -> {
                    success("clashed_shorthand");
                })
                .build();
        commandService.registerCommand(spec6);
    }
}
