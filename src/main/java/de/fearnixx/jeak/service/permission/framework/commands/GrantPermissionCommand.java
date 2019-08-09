package de.fearnixx.jeak.service.permission.framework.commands;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.CommandException;
import de.fearnixx.jeak.service.command.CommandParameterException;
import de.fearnixx.jeak.service.command.ICommandContext;
import de.fearnixx.jeak.service.command.ICommandReceiver;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Optional;

public class GrantPermissionCommand implements ICommandReceiver {

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
            throw new CommandException("Sorry, your client could not be found in the cache. "
                    + "Please wait a moment and try again.");
        }

        if (!invoker.hasPermission("frw.permission.edit")) {
            throw new CommandException("You are not allowed to use this command! (UID: "
                    + invoker.getUniqueID() + ", failed at: frw.permission.edit)");
        }

        if (ctx.getArguments().size() < 2 || ctx.getArguments().size() > 3) {
            throw new CommandException("At least two and at most three parameters are expected: "
                    + "<group-name> <permission_name> [value]");
        }

        final String groupName = ctx.getArguments().get(0);
        final Optional<IGroup> optGroup = permService.getFrameworkProvider().findGroupByName(groupName);
        if (optGroup.isEmpty()) {
            throw new CommandParameterException("The given group does not exists!", "group-name", groupName);
        }

        int value = 1;
        if (ctx.getArguments().size() == 3) {
            value = Integer.parseInt(ctx.getArguments().get(2));
        }

        String permName = ctx.getArguments().get(1);
        boolean revoke = permName.charAt(0) == '-';
        if (revoke) {
            permName = permName.substring(1);
        }


        if (revoke) {
            optGroup.get().removePermission(permName);
            server.getConnection().sendRequest(invoker.sendMessage("Permission removed."));
        } else {
            optGroup.get().setPermission(permName, value);
            server.getConnection().sendRequest(invoker.sendMessage("Permission set."));
        }
    }
}
