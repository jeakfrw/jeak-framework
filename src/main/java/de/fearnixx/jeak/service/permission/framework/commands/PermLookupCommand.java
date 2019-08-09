package de.fearnixx.jeak.service.permission.framework.commands;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.CommandException;
import de.fearnixx.jeak.service.command.CommandParameterException;
import de.fearnixx.jeak.service.command.ICommandContext;
import de.fearnixx.jeak.service.command.ICommandReceiver;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

            logger.info("Perm lookup for: {} with {} -> {}", client, lookupStr, resultStr);
            server.getConnection().sendRequest(client.sendMessage("Lookup result: " + resultStr));
            return;
        }

        throw new CommandException("Unknown arguments! Usage: "
                + "\"!permuuid-lookup [g:<group_name>|u:<user_name>|sg:<servergroup_id>]");
    }
}
