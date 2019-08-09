package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IRawQueryEvent.IMessage;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission.PriorityType;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.BlockingRequest;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Helpful documentation if you want to understand TS3 perms a little better.
 * * http://media.teamspeak.com/ts3_literature/TeamSpeak%203%20Permissions%20Guide.txt
 */
@FrameworkService(serviceInterface = ITS3PermissionProvider.class)
public class QueryPermissionProvider extends AbstractTS3PermissionProvider implements ITS3PermissionProvider {

    public static final Integer CACHE_TIMEOUT_SECONDS = 90;
    public static final Integer EMPTY_RESULT_ID = 1281;

    private static final Logger logger = LoggerFactory.getLogger(QueryPermissionProvider.class);

    private final Map<Integer, TS3PermCache> clientPerms = new HashMap<>();
    private final Map<Integer, TS3PermCache> channelPerms = new HashMap<>();
    private final Map<Integer, TS3PermCache> channelGroupPerms = new HashMap<>();
    private final Map<Integer, TS3PermCache> serverGroupPerms = new HashMap<>();
    private final Map<Integer, Map<Integer, TS3PermCache>> channelClientPerms = new HashMap<>();

    @Override
    public void clearCache(PriorityType type, Integer optClientOrGroupID, Integer optChannelID) {

        switch (type) {
            case CLIENT:
                if (optClientOrGroupID == null) {
                    throw new IllegalArgumentException("ClientID missing");
                }
                clientPerms.remove(optClientOrGroupID);
                break;
            case CHANNEL:
                if (optChannelID == null) {
                    throw new IllegalArgumentException("ChannelID missing");
                }
                channelPerms.remove(optChannelID);
                break;
            case SERVER_GROUP:
                if (optClientOrGroupID == null) {
                    throw new IllegalArgumentException("Server group ID missing");
                }
                serverGroupPerms.remove(optClientOrGroupID);
                break;
            case CHANNEL_GROUP:
                if (optClientOrGroupID == null) {
                    throw new IllegalArgumentException("Channel group ID missing");
                }
                channelGroupPerms.remove(optClientOrGroupID);
                break;
            case CHANNEL_CLIENT:
                if (optClientOrGroupID == null) {
                    throw new IllegalArgumentException("Client ID missing");
                }
                if (optChannelID == null) {
                    throw new IllegalArgumentException("Channel ID missing");
                }
                Map<Integer, TS3PermCache> clients = channelClientPerms.getOrDefault(optChannelID, null);
                if (clients != null) {
                    clients.remove(optClientOrGroupID);
                }
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
            BlockingRequest request = new BlockingRequest(req);
            getServer().getConnection().sendRequest(req);
            if (request.waitForCompletion()) {
                answer = ((IMessage.IAnswer) request.getAnswer().getRawReference());
                cache = new TS3PermCache();
                cache.setResponse(answer);
                clientPerms.put(clientDBID, cache);
            } else {
                logger.warn("Permission lookup for \"{}\" on client \"{}\" did not complete.", permSID, clientDBID);
                return Optional.empty();
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
            BlockingRequest request = new BlockingRequest(req);
            getServer().getConnection().sendRequest(req);
            if (request.waitForCompletion()) {
                answer = ((IMessage.IAnswer) request.getAnswer().getRawReference());
                cache = new TS3PermCache();
                cache.setResponse(answer);
                serverGroupPerms.put(serverGroupID, cache);
            } else {
                logger.warn("Permission lookup for \"{}\" on server group \"{}\" did not complete.", permSID, serverGroupID);
                return Optional.empty();
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
            BlockingRequest request = new BlockingRequest(req);
            getServer().getConnection().sendRequest(req);
            if (request.waitForCompletion()) {
                answer = ((IMessage.IAnswer) request.getAnswer().getRawReference());
                cache = new TS3PermCache();
                cache.setResponse(answer);
                channelGroupPerms.put(channelGroupID, cache);

            } else {
                logger.warn("Permission lookup for \"{}\" on channel group \"{}\" did not complete.", permSID, channelGroupID);
                return Optional.empty();
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL_GROUP);
    }

    @Override
    public Optional<ITS3Permission> getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID) {
        IMessage.IAnswer answer = null;
        Map<Integer, TS3PermCache> channelClientMap = channelClientPerms.getOrDefault(channelID, null);
        TS3PermCache cache;
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
            BlockingRequest request = new BlockingRequest(req);
            getServer().getConnection().sendRequest(req);
            if (request.waitForCompletion()) {
                answer = ((IMessage.IAnswer) request.getAnswer());
                cache = new TS3PermCache();
                cache.setResponse(answer);
                if (channelClientMap == null) {
                    channelClientMap = new HashMap<>();
                    channelClientPerms.put(channelID, channelClientMap);
                }
                channelClientMap.put(clientDBID, cache);
            } else {
                logger.warn("Permission lookup for \"{}\" on channel \"{}\" for client \"{}\" did not complete.", permSID, channelID, clientDBID);
                return Optional.empty();
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
            BlockingRequest request = new BlockingRequest(req);
            getServer().getConnection().sendRequest(req);
            if (request.waitForCompletion()) {
                answer = ((IMessage.IAnswer) request.getAnswer());
                cache = new TS3PermCache();
                cache.setResponse(answer);
                channelPerms.put(channelID, cache);
            } else {
                logger.warn("Permission lookup for \"{}\" on channel \"{}\" did not complete.", permSID, channelID);
                return Optional.empty();
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL);
    }

    protected Optional<ITS3Permission> permFromList(String permSID, IMessage.IAnswer answer,
                                                    ITS3Permission.PriorityType type) {

        if (answer != null && answer.getError().getCode().equals(EMPTY_RESULT_ID)) {
            return Optional.empty();
        }

        while (answer != null) {
            if (permSID.equals(answer.getProperty("permsid").orElse(null))) {
                break;
            }
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
}
