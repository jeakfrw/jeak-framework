package de.fearnixx.jeak.service.permission.framework.commands;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.*;
import de.fearnixx.jeak.service.command.spec.Commands;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.fearnixx.jeak.service.command.spec.Commands.argumentSpec;
import static de.fearnixx.jeak.service.command.spec.Commands.paramSpec;

public class PermLookupCommand implements ICommandReceiver {

    private static final Logger logger = LoggerFactory.getLogger(PermLookupCommand.class);

    @Inject
    private IServer server;

    @Inject
    private IUserService userService;

    @Inject
    private IDataCache dataCache;

    @Inject
    private IPermissionService permService;

    @Override
    public void receive(ICommandContext ctx) throws CommandException {
        Integer invokerId = ctx.getRawEvent().getInvokerId();
        IPermissionProvider frwProvider = permService.getFrameworkProvider();

        Optional<IClient> optClient = userService.getClientByID(invokerId);
        if (optClient.isEmpty()) {
            throw new CommandException("Could not find you in the cache. Please try again later.");
        }
        IClient client = optClient.get();

        if (ctx.getArguments().isEmpty()) {
            UUID permUUID = client.getUniqueID();
            logger.info("Self lookup for: {} -> {}", client, permUUID);
            server.getConnection().sendRequest(client.sendMessage("Your permission ID is: " + permUUID));
            return;
        }

        if (ctx.getArguments().size() == 1) {
            String lookupStr = ctx.getArguments().get(0);
            String lookupName;
            String resultStr;

            if (lookupStr.startsWith("g:")) {
                lookupName = lookupStr.substring(2);
                Optional<IGroup> optGroup = frwProvider.findGroupByName(lookupName);
                resultStr = optGroup.map(ISubject::getUniqueID).map(UUID::toString).orElse(null);

            } else if (lookupStr.startsWith("u:")) {
                lookupName = lookupStr.substring(2);
                Pattern pattern = Pattern.compile(lookupName);
                StringBuilder builder = new StringBuilder();
                dataCache.getClients()
                        .stream()
                        .filter(c -> pattern.matcher(c.getNickName()).find())
                        .map(c -> c.toString() + '{' + c.getUniqueID() + '}')
                        .forEach(res -> builder.append(res).append(','));

                resultStr = builder.toString();
                if (resultStr.length() > 1) {
                    resultStr = resultStr.substring(0, resultStr.length() - 1);
                }
                resultStr = '[' + resultStr + ']';

            } else if (lookupStr.startsWith("sg:")) {
                lookupName = lookupStr.substring(3);
                int sgid = Integer.parseInt(lookupName);
                List<IGroup> grps = frwProvider.getGroupsLinkedToServerGroup(sgid);
                StringBuilder builder = new StringBuilder();
                grps.stream()
                        .map(g -> g.getName() + '{' + g.getUniqueID() + '}')
                        .forEach(res -> builder.append(res).append(','));

                resultStr = builder.toString();
                if (resultStr.length() > 1) {
                    resultStr = resultStr.substring(0, resultStr.length() - 1);
                }
                resultStr = '[' + resultStr + ']';

            } else {
                throw new CommandParameterException("Unknown lookup classification!", "search", lookupStr);
            }

            sendResult(client, lookupStr, resultStr);
            return;
        }

        throw new CommandException("Unknown arguments! Usage: \"!permuuid-lookup [g:<group_name>|u:<user_name>|sg:<servergroup_id>]");
    }

    private void sendResult(IClient client, String lookupStr, String resultStr) {
        logger.info("Perm lookup for: {} with {} -> {}", client, lookupStr, resultStr);
        server.getConnection().sendRequest(client.sendMessage("Lookup result: " + resultStr));
    }

    private void typedInvoke(ICommandExecutionContext ctx) {
        Optional<IGroup> group = ctx.getOne("group", IGroup.class);
        Optional<IUser> user = ctx.getOne("user", IUser.class);
        Optional<IClient> client = ctx.getOne("client", IClient.class);
        Optional<Integer> sgid = ctx.getOne("sgid", Integer.class);
        if (group.isPresent()) {
            sendResult(ctx.getSender(), "group", group.get().getUniqueID().toString());
        } else if (user.isPresent()) {
            sendResult(ctx.getSender(), "user", user.get().toString() + "/" + user.get().getUniqueID().toString());
        } else if (client.isPresent()) {
            sendResult(ctx.getSender(), "client", client.get().toString() + "/" + client.get().getUniqueID().toString());
        } else if (sgid.isPresent()) {
            IPermissionProvider frwProvider = permService.getFrameworkProvider();
            List<IGroup> grps = frwProvider.getGroupsLinkedToServerGroup(sgid.get());
            String groupsStr = grps.stream()
                    .map(g -> g.getName() + "{" + g.getUniqueID().toString() + "}")
                    .collect(Collectors.joining(","));
            sendResult(ctx.getSender(), "sgid", "[" + groupsStr + "]");
        }
    }

    public ICommandSpec getCommandSpec() {
        return Commands.commandSpec("permuuid-lookup", "frw:permuuid-lookup")
                .parameters(
                        paramSpec().firstMatching(
                                paramSpec("group", IGroup.class),
                                paramSpec("user", IUser.class),
                                paramSpec("sgid", Integer.class)
                        )
                )
                .executor(this::typedInvoke)
                .build();
    }

    public ICommandSpec getArgumentCommandSpec() {
        return Commands.commandSpec("permuuid-lookup-arg", "frw:permuuid-lookup-arg")
                .arguments(
                        argumentSpec().firstMatching(
                                argumentSpec("group", "g", IGroup.class),
                                argumentSpec("user", "u", IUser.class),
                                argumentSpec("client", "c", IClient.class),
                                argumentSpec("sgid", "sg", Integer.class)
                        )
                )
                .executor(this::typedInvoke)
                .build();
    }
}
