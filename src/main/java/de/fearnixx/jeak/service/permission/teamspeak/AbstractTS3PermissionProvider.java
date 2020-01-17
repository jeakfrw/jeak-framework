package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTS3PermissionProvider implements ITS3PermissionProvider {

    private final PermIdCache permIdCache = new PermIdCache();

    @Inject
    private IUserService userService;

    @Inject
    private IServer server;

    @Inject
    private IInjectionService injectionService;

    @Inject
    private IEventService eventService;

    protected int getServerId() {
        return server.getInstanceId();
    }

    protected IServer getServer() {
        return server;
    }

    @Override
    public Optional<ITS3Permission> getActivePermission(String permSID, String clientTS3UniqueID) {
        Optional<IClient> optClient = userService.findClientByUniqueID(clientTS3UniqueID)
                .stream()
                .findFirst();
        final ITS3Permission[] perm = new ITS3Permission[]{null};
        optClient.ifPresent(c -> getActivePermission(c.getClientDBID(), permSID).ifPresent(p -> perm[0] = p));
        return Optional.ofNullable(perm[0]);
    }

    @Override
    public Optional<ITS3Permission> getActivePermission(Integer clientID, String permSID) {
        List<ITS3Permission> activeContext = getActiveContext(clientID, permSID);

        if (activeContext.isEmpty())
            return Optional.empty();

        boolean skipFlag = false;
        boolean negateFlag = false;
        ITS3Permission effective = null;
        Integer maxValue = 0;
        Integer maxWeight = 0;

        // We need to go through twice so all permissions are taken into account
        // Even when a later permission changes a flag.
        for (ITS3Permission perm : activeContext) {
            ITS3Permission.PriorityType type = perm.getPriorityType();

            if (perm.getNegate()) {
                negateFlag = true;
            }

            if (perm.getSkip() && (type == ITS3Permission.PriorityType.SERVER_GROUP || type == ITS3Permission.PriorityType.CLIENT)) {
                skipFlag = true;
            }
        }

        for (ITS3Permission perm : activeContext) {
            ITS3Permission.PriorityType type = perm.getPriorityType();
            Integer value = perm.getValue();

            if (skipFlag && (type == ITS3Permission.PriorityType.CHANNEL || type == ITS3Permission.PriorityType.CHANNEL_GROUP)) {
                continue;
            }

            if ((value >= maxValue || negateFlag)
                    && type.getWeight() > maxWeight) {
                effective = perm;
                maxValue = value;
                maxWeight = type.getWeight();
            }
        }

        return Optional.ofNullable(effective);
    }

    protected List<ITS3Permission> getActiveContext(Integer clientID, String permSID) {
        final List<ITS3Permission> result = new ArrayList<>();
        Optional<IClient> optClient = userService.getClientByID(clientID);
        IClient client = optClient.orElseThrow(() -> new IllegalStateException("Cannot check permissions: Given client ID is not online: " + clientID));

        client.getGroupIDs().forEach(gid -> getServerGroupPermission(gid, permSID).ifPresent(result::add));
        getClientPermission(client.getClientDBID(), permSID).ifPresent(result::add);
        getChannelGroupPermission(client.getChannelGroupID(), permSID).ifPresent(result::add);
        getChannelClientPermission(client.getChannelID(), client.getClientDBID(), permSID).ifPresent(result::add);
        getChannelPermission(client.getChannelID(), permSID).ifPresent(result::add);

        return result;
    }

    @Override
    public Integer translateSID(String permSID) {
        return permIdCache.getPermIdFor(permSID);
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        injectionService.injectInto(permIdCache);
        eventService.registerListener(permIdCache);
    }
}
