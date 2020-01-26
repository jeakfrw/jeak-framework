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

public class LinkGroupCommand implements ICommandReceiver {

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

        if (!invoker.hasPermission("frw.permission.link_group")) {
            throw new CommandException("You are not allowed to use this command! (UID: " + invoker.getUniqueID() + ", failed at: frw.permission.link_group)");
        }

        if (ctx.getArguments().size() != 2) {
            throw new CommandException("(Only/At least) Two parameters are expected: <group-name> <ts3-servergroup-id>");
        }

        final String desiredName = ctx.getArguments().get(0);

        final Optional<IGroup> optGroup = permService.getFrameworkProvider().findGroupByName(desiredName);
        if (optGroup.isEmpty()) {
            throw new CommandParameterException("The given group does not exists!", "group-name", desiredName);
        }

        final int serverGroupId = Integer.parseInt(ctx.getArguments().get(1));
        IGroup group = optGroup.get();
        linkGroupToServerGroup(invoker, serverGroupId, group);
    }

    private void linkGroupToServerGroup(IClient invoker, int serverGroupId, IGroup group) throws CommandException {
        if (group.linkServerGroup(serverGroupId)) {
            server.getConnection().sendRequest(invoker.sendMessage("Linked group \"" + group.getName() + "\" to sgID: " + serverGroupId));
        } else {
            throw new CommandException("Failed to link server group: " + group.getName() + " to server group id; " + serverGroupId);
        }
    }

    private void typedInvoke(ICommandExecutionContext ctx) throws CommandException {
        IGroup group = ctx.getRequiredOne("group", IGroup.class);
        int serverGroupId = ctx.getRequiredOne("sgid", Integer.class);
        linkGroupToServerGroup(ctx.getSender(), serverGroupId, group);
    }

    public ICommandSpec getCommandSpec() {
        return Commands.commandSpec("perm-group-link", "frw:perm-group-link")
                .parameters(
                        Commands.paramSpec("group", IGroup.class),
                        Commands.paramSpec("sgid", Integer.class)
                )
                .permission("frw.permission.link_group")
                .executor(this::typedInvoke)
                .build();
    }
}
