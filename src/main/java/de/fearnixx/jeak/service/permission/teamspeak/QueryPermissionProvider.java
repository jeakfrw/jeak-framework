package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IRawQueryEvent;
import de.fearnixx.jeak.event.IRawQueryEvent.IMessage;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission.PriorityType;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.except.QueryException;
import de.fearnixx.jeak.teamspeak.query.IQueryPromise;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 04-Feb-18
 *
 * Helpful documentation if you want to understand TS3 perms a little better:
 * * http://media.teamspeak.com/ts3_literature/TeamSpeak%203%20Permissions%20Guide.txt
 */
@FrameworkService(serviceInterface = ITS3PermissionProvider.class)
public class QueryPermissionProvider extends AbstractTS3PermissionProvider implements ITS3PermissionProvider {

    public static final Integer CACHE_TIMEOUT_SECONDS = 90;
    public static final Integer EMPTY_RESULT_ID = 1281;

    private static final Logger logger = LoggerFactory.getLogger(QueryPermissionProvider.class);

    private Map<String, Integer> permIDCache = new HashMap<>();
    private Map<Integer, TS3PermCache> clientPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> channelPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> channelGroupPerms = new HashMap<>();
    private Map<Integer, TS3PermCache> serverGroupPerms = new HashMap<>();
    private Map<Integer, Map<Integer, TS3PermCache>> channelClientPerms = new HashMap<>();

    @Override
    public void clearCache(PriorityType type, Integer optClientOrGroupID, Integer optChannelID) {

        switch (type) {
            case CLIENT:
                if (optClientOrGroupID == null)
                    throw new IllegalArgumentException("ClientID missing");
                clientPerms.remove(optClientOrGroupID);
                break;
            case CHANNEL:
                if (optChannelID == null)
                    throw new IllegalArgumentException("ChannelID missing");
                channelPerms.remove(optChannelID);
                break;
            case SERVER_GROUP:
                if (optClientOrGroupID == null)
                    throw new IllegalArgumentException("Server group ID missing");
                serverGroupPerms.remove(optClientOrGroupID);
                break;
            case CHANNEL_GROUP:
                if (optClientOrGroupID == null)
                    throw new IllegalArgumentException("Channel group ID missing");
                channelGroupPerms.remove(optClientOrGroupID);
                break;
            case CHANNEL_CLIENT:
                if (optClientOrGroupID == null)
                    throw new IllegalArgumentException("Client ID missing");
                if (optChannelID == null)
                    throw new IllegalArgumentException("Channel ID missing");
                Map<Integer, TS3PermCache> clients = channelClientPerms.getOrDefault(optChannelID, null);
                if (clients != null)
                    clients.remove(optClientOrGroupID);
                break;
            default:
                throw new IllegalArgumentException("Unknown PriorityType!");
        }
    }

    @Override
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
                                             .command(QueryCommands.PERMISSION.CLIENT_LIST_PERMISSIONS)
                                             .addKey("cldbid", clientDBID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = getServer().getConnection().promiseRequest(req);
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

    @Override
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
                                             .command(QueryCommands.PERMISSION.SERVERGROUP_LIST_PERMISSIONS)
                                             .addKey("sgid", serverGroupID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = getServer().getConnection().promiseRequest(req);
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

    @Override
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
                                             .command(QueryCommands.PERMISSION.CHANNEL_GROUP_PERMISSION_LIST)
                                             .addKey("cgid", channelGroupID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = getServer().getConnection().promiseRequest(req);
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

    @Override
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
                                             .command(QueryCommands.PERMISSION.CHANNEL_CLIENT_LIST_PERMISSIONS)
                                             .addKey(PropertyKeys.Channel.ID, channelID)
                                             .addKey("cldbid", clientDBID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = getServer().getConnection().promiseRequest(req);
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

    @Override
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
                                             .command(QueryCommands.PERMISSION.CHANNEL_LIST_PERMISSIONS)
                                             .addKey("cid", channelID)
                                             .addOption("-permsid")
                                             .build();
            IQueryPromise promise = getServer().getConnection().promiseRequest(req);
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
        logger.debug("Permission {} not found in list", permSID);
        return Optional.empty();
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
                return Optional.empty();
            }
        }
        return Optional.of(intID);
    }
}
