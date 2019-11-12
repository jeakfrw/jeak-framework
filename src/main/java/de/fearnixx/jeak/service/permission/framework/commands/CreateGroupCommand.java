package de.fearnixx.jeak.service.permission.framework.commands;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.*;
import de.fearnixx.jeak.service.command.spec.Commands;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Optional;

public class CreateGroupCommand implements ICommandReceiver {

    @Inject
    private IUserService userService;

    @Inject
    private IPermissionService permService;

    @Inject
    private IServer server;

    @Override
    public void receive(ICommandContext ctx) throws CommandException {
        final Integer invokerId = ctx.getRawEvent().getInvokerId();
        final IClient invoker = userService.getClientByID(invokerId).orElse(null);

        if (invoker == null) {
            throw new CommandException("Sorry, your client could not be found in the cache. Please wait a moment and try again.");
        }

        if (!invoker.hasPermission("frw.permission.create_group")) {
            throw new CommandException("You are not allowed to use this command! (UID: " + invoker.getUniqueID() + ", failed at: frw.permission.create_group)");
        }

        if (ctx.getArguments().size() != 1) {
            throw new CommandException("(Only/At least) One parameter is expected: group-name");
        }

        final String desiredName = ctx.getArguments().get(0);

        createGroup(invoker, desiredName);
    }

    private void createGroup(IClient invoker, String desiredName) throws CommandException {
        if (permService.getFrameworkProvider().findGroupByName(desiredName).isPresent()) {
            throw new CommandParameterException("The given group already exists!", "group-name", desiredName);
        }

        final Optional<IGroup> optParent = permService.getFrameworkProvider().createParent(desiredName);
        if (optParent.isPresent()) {
            server.getConnection().sendRequest(invoker.sendMessage("Group created successfully: " + desiredName));
        } else {
            throw new CommandException("Failed to create group. Consult logs for more information.");
        }
    }

    private void typedInvoke(ICommandExecutionContext ctx) throws CommandException {
        String name = ctx.getRequiredOne("name", String.class);
        createGroup(ctx.getSender(), name);
    }

    public ICommandSpec getCommandSpec() {
        return Commands.commandSpec("perm-group-create", "frw:perm-group-create")
                .parameters(Commands.paramSpec("name", String.class))
                .permission("frw.permission.create_group")
                .executor(this::typedInvoke)
                .build();
    }
}
