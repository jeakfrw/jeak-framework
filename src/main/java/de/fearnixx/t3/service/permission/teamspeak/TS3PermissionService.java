package de.fearnixx.t3.service.permission.teamspeak;

import de.fearnixx.t3.event.IRawQueryEvent.IMessage;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.teamspeak.IServer;
import de.fearnixx.t3.teamspeak.PropertyKeys;
import de.fearnixx.t3.teamspeak.cache.IDataCache;
import de.fearnixx.t3.teamspeak.data.IClient;
import de.fearnixx.t3.teamspeak.query.IQueryPromise;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;
import de.fearnixx.t3.teamspeak.query.PromisedRequest;
import de.fearnixx.t3.teamspeak.query.except.QueryException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 04-Feb-18
 */
public class TS3PermissionService implements ITS3PermissionService {

    public static final Integer CACHE_TIMEOUT_SECONDS = 60;

    @Inject
    public IDataCache dataCache;

    @Inject
    public IServer server;

    private Map<Integer, TS3PermCache> clientPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> channelPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> channelGroupPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> serverGroupPerms = new HashMap<>();
    private Map<Integer, Map<Integer, TS3PermCache>> channelClientPerms = new HashMap<>();

    @Override
    public Optional<ITS3Permission> getActivePermission(Integer clientID, String permSID) {
        List<ITS3Permission> activeKontext = getActiveContext(clientID, permSID);

    }

    public List<ITS3Permission> getActiveContext(Integer clientID, String permSID) {
        final List<ITS3Permission> result = new ArrayList<>();

        IClient client = dataCache.getClientMap().get(clientID);
        client.getGroupIDs().forEach(gid -> result.add(getServerGroupPermission(gid, permSID)));

        result.add(getClientPermission(client.getClientDBID(), permSID));
        result.add(getChannelGroupPermission(client.getChannelGroupID(), permSID));
        result.add(getChannelClientPermission(client.getChannelID(), clientID, permSID));
        result.add(getChannelPermission(client.getChannelID(), permSID));
    }

    public ITS3Permission getClientPermission(Integer clientDBID, String permSID) {
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
                                             .addKey(PropertyKeys.Client.DBID, clientDBID)
                                             .addOption("permsid")
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

    public ITS3Permission getServerGroupPermission(Integer serverGroupID, String permSID) {
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
                                             .addOption("permsid")
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

    public ITS3Permission getChannelGroupPermission(Integer channelGroupID, String permSID) {
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
                                             .addOption("permsid")
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

    public ITS3Permission getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID) {
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
                                             .addKey(PropertyKeys.Client.DBID, clientDBID)
                                             .addOption("permsid")
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

    public ITS3Permission getChannelPermission(Integer channelID, String permSID) {
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
                                             .addKey("cgid", channelID)
                                             .addOption("permsid")
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

    protected ITS3Permission permFromList(String permSID, IMessage.IAnswer answer, ITS3Permission.PriorityType type) {

        while (answer != null) {
            if (permSID.equals(answer.getProperty("permsid").orElse(null)))
                break;
            answer = ((IMessage.IAnswer) answer.getNext());
        }

        if (answer == null)
            throw new QueryException("PermSID " + permSID + " not found in response!");

        TS3Permission perm = new TS3Permission(type, permSID);
        perm.copyFrom(answer);
        return perm;
    }
}
