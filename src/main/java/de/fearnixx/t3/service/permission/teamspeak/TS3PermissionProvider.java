package de.fearnixx.t3.service.permission.teamspeak;

import de.fearnixx.t3.event.IRawQueryEvent;
import de.fearnixx.t3.event.IRawQueryEvent.IMessage;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.service.permission.base.IPermission;
import de.fearnixx.t3.teamspeak.IServer;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.cache.IDataCache;
import de.fearnixx.t3.teamspeak.data.IClient;
import de.fearnixx.t3.teamspeak.query.IQueryPromise;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.except.QueryException;
import de.fearnixx.t3.service.permission.teamspeak.ITS3Permission.PriorityType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 04-Feb-18
 *
 * Helpful documentation if you want to understand TS3 perms a little better:
 * * http://media.teamspeak.com/ts3_literature/TeamSpeak%203%20Permissions%20Guide.txt
 */
public class TS3PermissionProvider implements ITS3PermissionProvider {

    public static final Integer CACHE_TIMEOUT_SECONDS = 60;
    public static final Integer EMPTY_RESULT_ID = 1281;

    @Inject
    public IDataCache dataCache;

    @Inject
    public IServer server;

    private Map<String, Integer> permIDCache = new HashMap<>();
    private Map<Integer, TS3PermCache> clientPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> channelPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> channelGroupPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> serverGroupPerms = new HashMap<>();
    private Map<Integer, Map<Integer, TS3PermCache>> channelClientPerms = new HashMap<>();

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
            PriorityType type = perm.getPriorityType();

            if (perm.getNegate()) {
                negateFlag = true;
            }

            if (perm.getSkip() && (type == PriorityType.SERVER_GROUP || type == PriorityType.CLIENT)) {
                skipFlag = true;
            }
        }

        for (ITS3Permission perm : activeContext) {
            PriorityType type = perm.getPriorityType();
            Integer value = perm.getValue();

            if (skipFlag && (type == PriorityType.CHANNEL || type == PriorityType.CHANNEL_GROUP)) {
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

    public List<ITS3Permission> getActiveContext(Integer clientID, String permSID) {
        final List<ITS3Permission> result = new ArrayList<>();
        IClient client = dataCache.getClientMap().get(clientID);

        client.getGroupIDs().forEach(gid -> getServerGroupPermission(gid, permSID).ifPresent(result::add));
        getClientPermission(client.getClientDBID(), permSID).ifPresent(result::add);
        getChannelGroupPermission(client.getChannelGroupID(), permSID).ifPresent(result::add);
        getChannelClientPermission(client.getChannelID(), client.getClientDBID(), permSID).ifPresent(result::add);
        getChannelPermission(client.getChannelID(), permSID).ifPresent(result::add);

        return result;
    }

    public Optional<ITS3Permission> getClientPermission(Integer clientDBID, String permSID) {
        IMessage.IAnswer answer = null;
        TS3PermCache cache = clientPerms.getOrDefault(clientDBID, null);
        if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
            LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

            if (cache.getTimestamp().isAfter(cacheLimit)) {
                answer = cache.getAnswer();
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                                             .command("clientpermlist")
                                             .addKey("cldbid", clientDBID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = server.getConnection().promiseRequest(req);
            try {
                answer = promise.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new QueryException("Failed to lookup client permission " + permSID, e);
            }
            if (answer != null) {
                cache = new TS3PermCache(clientDBID, null, ITS3Permission.PriorityType.CLIENT);
                cache.setResponse(answer);
                clientPerms.put(clientDBID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CLIENT);
    }

    public Optional<ITS3Permission> getServerGroupPermission(Integer serverGroupID, String permSID) {
        IMessage.IAnswer answer = null;
        TS3PermCache cache = serverGroupPerms.getOrDefault(serverGroupID, null);
        if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
            LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

            if (cache.getTimestamp().isAfter(cacheLimit)) {
                answer = cache.getAnswer();
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                                             .command("servergrouppermlist")
                                             .addKey("sgid", serverGroupID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = server.getConnection().promiseRequest(req);
            try {
                answer = promise.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new QueryException("Failed to lookup server group permission " + permSID, e);
            }
            if (answer != null) {
                cache = new TS3PermCache(serverGroupID, null, ITS3Permission.PriorityType.SERVER_GROUP);
                cache.setResponse(answer);
                serverGroupPerms.put(serverGroupID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.SERVER_GROUP);
    }

    public Optional<ITS3Permission> getChannelGroupPermission(Integer channelGroupID, String permSID) {
        IMessage.IAnswer answer = null;
        TS3PermCache cache = channelGroupPerms.getOrDefault(channelGroupID, null);
        if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
            LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

            if (cache.getTimestamp().isAfter(cacheLimit)) {
                answer = cache.getAnswer();
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                                             .command("channelgrouppermlist")
                                             .addKey("cgid", channelGroupID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = server.getConnection().promiseRequest(req);
            try {
                answer = promise.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new QueryException("Failed to lookup channel group permission " + permSID, e);
            }
            if (answer != null) {
                cache = new TS3PermCache(channelGroupID, null, ITS3Permission.PriorityType.CHANNEL_GROUP);
                cache.setResponse(answer);
                channelGroupPerms.put(channelGroupID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL_GROUP);
    }

    public Optional<ITS3Permission> getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID) {
        IMessage.IAnswer answer = null;
        Map<Integer, TS3PermCache> channelClientMap = channelClientPerms.getOrDefault(channelID, null);
        TS3PermCache cache = null;
        if (channelClientMap != null) {
            cache = channelClientMap.getOrDefault(clientDBID, null);
            if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
                LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

                if (cache.getTimestamp().isAfter(cacheLimit)) {
                    answer = cache.getAnswer();
                }
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                                             .command("channelclientpermlist")
                                             .addKey(PropertyKeys.Channel.ID, channelID)
                                             .addKey("cldbid", clientDBID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = server.getConnection().promiseRequest(req);
            try {
                answer = promise.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new QueryException("Failed to lookup client permission " + permSID, e);
            }
            if (answer != null) {
                cache = new TS3PermCache(clientDBID, null, ITS3Permission.PriorityType.CHANNEL_CLIENT);
                cache.setResponse(answer);
                if (channelClientMap == null) {
                    channelClientMap = new HashMap<>();
                    channelClientPerms.put(channelID, channelClientMap);
                }
                channelClientMap.put(clientDBID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL_CLIENT);
    }

    public Optional<ITS3Permission> getChannelPermission(Integer channelID, String permSID) {
        IMessage.IAnswer answer = null;
        TS3PermCache cache = channelPerms.getOrDefault(channelID, null);
        if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
            LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

            if (cache.getTimestamp().isAfter(cacheLimit)) {
                answer = cache.getAnswer();
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                                             .command("channelpermlist")
                                             .addKey("cid", channelID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = server.getConnection().promiseRequest(req);
            try {
                answer = promise.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new QueryException("Failed to lookup channel permission " + permSID, e);
            }
            if (answer != null) {
                cache = new TS3PermCache(channelID, null, ITS3Permission.PriorityType.CHANNEL);
                cache.setResponse(answer);
                channelPerms.put(channelID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL);
    }

    protected Optional<ITS3Permission> permFromList(String permSID, IMessage.IAnswer answer, ITS3Permission.PriorityType type) {

        if (answer != null && answer.getError().getCode().equals(EMPTY_RESULT_ID)) {
            return Optional.empty();
        }

        while (answer != null) {
            if (permSID.equals(answer.getProperty("permsid").orElse(null)))
                break;
            answer = ((IMessage.IAnswer) answer.getNext());
        }

        if (answer != null) {
            TS3Permission perm = new TS3Permission(type, permSID);
            perm.copyFrom(answer);
            return Optional.of(perm);
        }
        return Optional.empty();
    }

    protected Optional<Integer> retrievePermIntID(String permSID) {
        Integer intID = permIDCache.getOrDefault(permSID, -1);
        if (intID == -1) {
            IQueryRequest request = IQueryRequest.builder()
                                                 .command("permidgetbyname")
                                                 .addKey("permsid", permSID)
                                                 .build();
            IQueryPromise promise = server.getConnection().promiseRequest(request);
            try {
                IRawQueryEvent.IMessage.IAnswer answer = promise.get(3, TimeUnit.SECONDS);
                intID = Integer.parseInt(answer.getProperty("permid").get());
                if (intID >= 0)
                    permIDCache.put(permSID, intID);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.of(intID);
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
}
