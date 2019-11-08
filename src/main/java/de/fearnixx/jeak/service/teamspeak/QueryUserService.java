package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.teamspeak.data.TS3User;
import de.fearnixx.jeak.teamspeak.query.BlockingRequest;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@FrameworkService(serviceInterface = IUserService.class)
public class QueryUserService extends AbstractUserService {

    private static final Logger logger = LoggerFactory.getLogger(QueryUserService.class);

    @Inject
    private IDataCache dataCache;

    @Inject
    private IServer server;

    @Override
    public List<IUser> findUserByUniqueID(String ts3uniqueID) {
        List<IClient> onlineClients = findClientByUniqueID(ts3uniqueID);
        if (!onlineClients.isEmpty()) {
            return new LinkedList<>(onlineClients);
        }

        IQueryRequest request = IQueryRequest.builder()
                .command("clientdbfind")
                .addKey("pattern", ts3uniqueID)
                .addOption("-uid")
                .build();

        BlockingRequest blockingRequest = new BlockingRequest(request);
        server.getConnection().sendRequest(request);
        if (!blockingRequest.waitForCompletion()) {
            logger.warn("Failed to get client DB ID (by uid) from blocking request.");
            return Collections.emptyList();
        }

        IQueryEvent.IAnswer answer = blockingRequest.getAnswer();
        if (answer.getErrorCode() != 0) {
            logger.warn("Error while getting client DB ID (by uid): {} - {}", answer.getErrorCode(), answer.getErrorMessage());
            return Collections.emptyList();
        }

        return findUsersFromSearchAnswer(answer);
    }

    @Override
    public List<IUser> findUserByDBID(int ts3dbID) {
        List<IClient> onlineClients = findClientByDBID(ts3dbID);
        if (!onlineClients.isEmpty()) {
            return new LinkedList<>(onlineClients);
        }

        IQueryRequest request = IQueryRequest.builder()
                .command("clientdbinfo")
                .addKey("cldbid", ts3dbID)
                .build();

        BlockingRequest blockingRequest = new BlockingRequest(request);
        server.getConnection().sendRequest(request);
        if (!blockingRequest.waitForCompletion()) {
            logger.warn("Failed to get user from blocking request.");
            return Collections.emptyList();
        }

        IQueryEvent.IAnswer answer = blockingRequest.getAnswer();
        if (answer.getErrorCode() != 0) {
            logger.warn("Error getting client from db request: {} - {}", answer.getErrorCode(), answer.getErrorMessage());
            return Collections.emptyList();
        }

        List<IUser> result = new LinkedList<>();
        answer.getDataChain()
                .stream()
                .map(data -> {
                    TS3User user = new TS3User();
                    user.copyFrom(data);
                    discoverServerGroups(user);
                    applyPermissions(user);
                    return user;
                })
                .forEach(result::add);
        return result;
    }

    private void discoverServerGroups(TS3User user) {
        IQueryRequest sgDiscoverRequest = IQueryRequest.builder()
                .command("servergroupsbyclientid")
                .addKey("cldbid", user.getClientDBID())
                .build();

        BlockingRequest request = new BlockingRequest(sgDiscoverRequest);
        server.getConnection().sendRequest(sgDiscoverRequest);
        if (!request.waitForCompletion()) {
            logger.error("Could not retrieve server groups of user: {}!", user);
            user.setProperty(PropertyKeys.Client.GROUPS, "");
            return;
        } else {
            IQueryEvent.IAnswer answer = request.getAnswer();
            String groups = answer.getDataChain()
                    .stream()
                    .map(holder -> holder.getProperty("sgid"))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.joining(","));
            user.setProperty(PropertyKeys.Client.GROUPS, groups);
        }
    }

    @Override
    public List<IUser> findUserByNickname(String ts3nickname) {
        List<IClient> onlineClients = findClientByNickname(ts3nickname);
        if (!onlineClients.isEmpty()) {
            return new LinkedList<>(onlineClients);
        }

        IQueryRequest request = IQueryRequest.builder()
                .command("clientdbfind")
                .addKey("pattern", ts3nickname)
                .build();

        BlockingRequest blockingRequest = new BlockingRequest(request);
        server.getConnection().sendRequest(request);
        if (!blockingRequest.waitForCompletion()) {
            logger.warn("Failed to get client DB ID (by nickname) from blocking request.");
            return Collections.emptyList();
        }

        IQueryEvent.IAnswer answer = blockingRequest.getAnswer();
        if (answer.getErrorCode() != 0) {
            logger.warn("Error while getting client DB ID (by nickname): {} - {}", answer.getErrorCode(), answer.getErrorMessage());
            return Collections.emptyList();
        }

        return findUsersFromSearchAnswer(answer);
    }

    private List<IUser> findUsersFromSearchAnswer(IQueryEvent.IAnswer answer) {
        final List<IUser> results = new LinkedList<>();
        answer.getDataChain()
                .stream()
                .map(holder -> holder.getProperty("cldbid").orElse(null))
                .filter(Objects::nonNull)
                .map(Integer::parseInt)
                .map(this::findUserByDBID)
                .forEach(results::addAll);
        return results;
    }
}
