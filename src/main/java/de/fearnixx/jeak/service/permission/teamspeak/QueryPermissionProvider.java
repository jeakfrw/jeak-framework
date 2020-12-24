package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Permission.PriorityType;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by MarkL4YG on 04-Feb-18
 * <p>
 * Helpful documentation if you want to understand TS3 perms a little better:
 * * http://media.teamspeak.com/ts3_literature/TeamSpeak%203%20Permissions%20Guide.txt
 */
@FrameworkService(serviceInterface = ITS3PermissionProvider.class)
public class QueryPermissionProvider extends AbstractTS3PermissionProvider implements ITS3PermissionProvider {

    public static final Integer CACHE_TIMEOUT_SECONDS = 90;
    public static final Integer EMPTY_RESULT_ID = 1281;

    private static final Logger logger = LoggerFactory.getLogger(QueryPermissionProvider.class);
    public static final String OPTION_PERMSID = "-" + PropertyKeys.Permission.STRING_ID;

    private final Map<Integer, TS3PermCache> clientPerms = new HashMap<>();
    private final Map<Integer, TS3PermCache> channelPerms = new HashMap<>();
    private final Map<Integer, TS3PermCache> channelGroupPerms = new HashMap<>();
    private final Map<Integer, TS3PermCache> serverGroupPerms = new HashMap<>();
    private final Map<Integer, Map<Integer, TS3PermCache>> channelClientPerms = new HashMap<>();

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
        IQueryEvent.IAnswer answer = null;
        synchronized (clientPerms) {
            TS3PermCache cache = clientPerms.getOrDefault(clientDBID, null);
            if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
                LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

                if (cache.getTimestamp().isAfter(cacheLimit)) {
                    answer = cache.getAnswer();
                }
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                    .command(QueryCommands.PERMISSION.CLIENT_LIST_PERMISSIONS)
                    .addKey(PropertyKeys.Client.DBID_S, clientDBID)
                    .addOption(OPTION_PERMSID)
                    .build();
            try {
                answer = getServer().getQueryConnection().promiseRequest(req).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while querying client permissions.", e);
                return Optional.empty();

            } catch (ExecutionException e) {
                logger.error("Error querying client permissions!", e);
                return Optional.empty();

            } catch (TimeoutException e) {
                logger.warn("Timed out querying client permissions! Is the connection overloaded?");
                return Optional.empty();
            }
            final var cache = new TS3PermCache();
            cache.setResponse(answer);
            synchronized (clientPerms) {
                clientPerms.put(clientDBID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CLIENT);
    }

    @Override
    public Optional<ITS3Permission> getServerGroupPermission(Integer serverGroupID, String permSID) {
        IQueryEvent.IAnswer answer = null;
        synchronized (serverGroupPerms) {
            TS3PermCache cache = serverGroupPerms.getOrDefault(serverGroupID, null);
            if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
                LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

                if (cache.getTimestamp().isAfter(cacheLimit)) {
                    answer = cache.getAnswer();
                }
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                    .command(QueryCommands.PERMISSION.SERVERGROUP_LIST_PERMISSIONS)
                    .addKey("sgid", serverGroupID)
                    .addOption(OPTION_PERMSID)
                    .build();
            try {
                answer = getServer().getQueryConnection().promiseRequest(req).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted getting server group permissions.", e);
                Thread.currentThread().interrupt();
                return Optional.empty();

            } catch (ExecutionException e) {
                logger.error("Error getting server group permissions!", e);
                return Optional.empty();

            } catch (TimeoutException e) {
                logger.error("Timed out getting server group permissions!");
                return Optional.empty();
            }

            final var cache = new TS3PermCache();
            cache.setResponse(answer);
            synchronized (serverGroupPerms) {
                serverGroupPerms.put(serverGroupID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.SERVER_GROUP);
    }

    @Override
    public Optional<ITS3Permission> getChannelGroupPermission(Integer channelGroupID, String permSID) {
        IQueryEvent.IAnswer answer = null;
        synchronized (channelGroupPerms) {
            TS3PermCache cache = channelGroupPerms.getOrDefault(channelGroupID, null);
            if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
                LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

                if (cache.getTimestamp().isAfter(cacheLimit)) {
                    answer = cache.getAnswer();
                }
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                    .command(QueryCommands.PERMISSION.CHANNEL_GROUP_PERMISSION_LIST)
                    .addKey("cgid", channelGroupID)
                    .addOption(OPTION_PERMSID)
                    .build();
            try {
                answer = getServer().getQueryConnection().promiseRequest(req).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted getting channel group permissions!", e);
                Thread.currentThread().interrupt();
                return Optional.empty();

            } catch (ExecutionException e) {
                logger.error("Error getting channel group permissions!", e);
                return Optional.empty();

            } catch (TimeoutException e) {
                logger.error("Timed out getting channel group permission! Is the connection overloaded?");
                return Optional.empty();
            }

            final var cache = new TS3PermCache();
            cache.setResponse(answer);
            synchronized (channelGroupPerms) {
                channelGroupPerms.put(channelGroupID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL_GROUP);
    }

    @Override
    public Optional<ITS3Permission> getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID) {
        IQueryEvent.IAnswer answer = null;
        synchronized (channelClientPerms) {
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
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                    .command(QueryCommands.PERMISSION.CHANNEL_CLIENT_LIST_PERMISSIONS)
                    .addKey(PropertyKeys.Channel.ID, channelID)
                    .addKey(PropertyKeys.Client.DBID_S, clientDBID)
                    .addOption(OPTION_PERMSID)
                    .build();

            try {
                answer = getServer().getQueryConnection().promiseRequest(req).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted getting channel client permissions.", e);
                Thread.currentThread().interrupt();
                return Optional.empty();

            } catch (ExecutionException e) {
                logger.error("Error getting channel client permissions!", e);
                return Optional.empty();

            } catch (TimeoutException e) {
                logger.error("Timed out getting channel client permissions! Is the connection overloaded?");
                return Optional.empty();
            }

            final var cache = new TS3PermCache();
            cache.setResponse(answer);
            synchronized (channelClientPerms) {
                var channelClientMap =
                        channelClientPerms.computeIfAbsent(channelID, id -> new ConcurrentHashMap<>());
                channelClientMap.put(clientDBID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL_CLIENT);
    }

    @Override
    public Optional<ITS3Permission> getChannelPermission(Integer channelID, String permSID) {
        IQueryEvent.IAnswer answer = null;
        synchronized (channelPerms) {
            TS3PermCache cache = channelPerms.getOrDefault(channelID, null);
            if (cache != null && CACHE_TIMEOUT_SECONDS > 0) {
                LocalDateTime cacheLimit = LocalDateTime.now().minusSeconds(CACHE_TIMEOUT_SECONDS);

                if (cache.getTimestamp().isAfter(cacheLimit)) {
                    answer = cache.getAnswer();
                }
            }
        }

        if (answer == null) {
            IQueryRequest req = IQueryRequest.builder()
                    .command(QueryCommands.PERMISSION.CHANNEL_LIST_PERMISSIONS)
                    .addKey(PropertyKeys.Channel.ID, channelID)
                    .addOption(OPTION_PERMSID)
                    .build();
            try {
                answer = getServer().getQueryConnection().promiseRequest(req).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted getting channel permission.", e);
                return Optional.empty();

            } catch (ExecutionException e) {
                logger.error("Error getting channel permission!", e);
                return Optional.empty();

            } catch (TimeoutException e) {
                logger.error("Timeout getting channel permission! Is the connection overloaded?");
                return Optional.empty();
            }

            final var cache = new TS3PermCache();
            cache.setResponse(answer);
            synchronized (channelPerms) {
                channelPerms.put(channelID, cache);
            }
        }
        return permFromList(permSID, answer, ITS3Permission.PriorityType.CHANNEL);
    }

    protected Optional<ITS3Permission> permFromList(String permSID, IQueryEvent.IAnswer answer, ITS3Permission.PriorityType type) {
        if (answer == null || !answer.getError().getCode().equals(EMPTY_RESULT_ID)) {
            return Optional.empty();
        }

        return answer.getDataChain()
                .stream()
                .filter(c -> permSID.equals(c.getProperty(PropertyKeys.Permission.STRING_ID).orElse(null)))
                .findFirst()
                .map(permHolder -> {
                    final var perm = new TS3Permission(type, permSID);
                    perm.copyFrom(permHolder);
                    return perm;
                });
    }
}
