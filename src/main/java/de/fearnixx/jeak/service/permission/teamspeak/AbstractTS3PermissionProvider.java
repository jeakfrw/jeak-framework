package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.query.IQueryPromise;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTS3PermissionProvider implements ITS3PermissionProvider {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTS3PermissionProvider.class);

    private final Map<String, Integer> permIDCache = new ConcurrentHashMap<>();

    @Inject
    private IDataCache dataCache;

    @Inject
    private IServer server;

    protected int getServerId() {
        return server.getInstanceId();
    }

    protected IServer getServer() {
        return server;
    }

    @Override
    public abstract void clearCache(ITS3Permission.PriorityType type, Integer optClientOrGroupID, Integer optChannelID);

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

            if ((value >= maxValue|| negateFlag)
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
        IClient client = dataCache.getClientMap().get(clientID);

        client.getGroupIDs().forEach(gid -> getServerGroupPermission(gid, permSID).ifPresent(result::add));
        getClientPermission(client.getClientDBID(), permSID).ifPresent(result::add);
        getChannelGroupPermission(client.getChannelGroupID(), permSID).ifPresent(result::add);
        getChannelClientPermission(client.getChannelID(), client.getClientDBID(), permSID).ifPresent(result::add);
        getChannelPermission(client.getChannelID(), permSID).ifPresent(result::add);

        return result;
    }

    @Override
    public Optional<IPermission> getPermission(String permSID, String clientUID) {
        Optional<IClient> optClient = dataCache.getClients()
                .stream()
                .filter(c -> c.getClientUniqueID().equals(clientUID))
                .findFirst();
        final IPermission[] perm = new IPermission[]{null};
        optClient.ifPresent(c -> getActivePermission(c.getClientDBID(), permSID).ifPresent(p -> perm[0] = p));
        return Optional.ofNullable(perm[0]);
    }

    protected Optional<Integer> retrievePermIntID(String permSID) {
        Integer intID = permIDCache.getOrDefault(permSID, -1);
        if (intID == -1) {
            IQueryRequest request = IQueryRequest.builder()
                    .command(QueryCommands.PERMISSION.PERMISSION_GET_ID_BYNAME)
                    .addKey("permsid", permSID)
                    .build();
            IQueryPromise promise = getServer().getConnection().promiseRequest(request);
            try {
                IRawQueryEvent.IMessage.IAnswer answer = promise.get(3, TimeUnit.SECONDS);
                intID = Integer.parseInt(answer.getProperty("permid").get());
                if (intID >= 0)
                    permIDCache.put(permSID, intID);
            } catch (Exception e) {
                logger.error("Failed to translate permSID!", e);
                return Optional.empty();
            }
        }
        return Optional.of(intID);
    }

    @Override
    public Integer translateSID(String permSID) {
        return retrievePermIntID(permSID)
                .orElseThrow(() -> new IllegalArgumentException("Untranslatable permSID: " + permSID));
    }
}