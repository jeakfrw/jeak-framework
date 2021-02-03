package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.teamspeak.data.TS3User;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@FrameworkService(serviceInterface = IUserService.class)
public class QueryUserService extends AbstractUserService {

    private static final Logger logger = LoggerFactory.getLogger(QueryUserService.class);

    @Inject
    private IServer server;

    @Override
    public List<IUser> findUserByUniqueID(String ts3uniqueID) {
        if (ts3uniqueID == null || ts3uniqueID.isBlank()) {
            throw new IllegalArgumentException("TS3 unique ID may not be null, blank or empty!");
        }

        List<IClient> onlineClients = findClientByUniqueID(ts3uniqueID);
        if (!onlineClients.isEmpty()) {
            return new LinkedList<>(onlineClients);
        }

        IQueryRequest request = IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_FIND_DB)
                .addKey("pattern", ts3uniqueID)
                .addOption("-uid")
                .build();

        final IQueryEvent.IAnswer answer;
        try {
            answer = server.getConnection().promiseRequest(request).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted finding user by unique ID.", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();

        } catch (ExecutionException e) {
            logger.error("Error finding user by unique ID!", e);
            return Collections.emptyList();

        } catch (TimeoutException e) {
            logger.error("Timed out finding user by unique ID! Is the connection overloaded?");
            return Collections.emptyList();
        }

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
                .command(QueryCommands.CLIENT.CLIENT_FIND_DB)
                .addKey(PropertyKeys.Client.DBID_S, ts3dbID)
                .build();

        IQueryEvent.IAnswer answer;
        try {
            answer = server.getConnection().promiseRequest(request).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted finding user by DBID.", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();

        } catch (ExecutionException e) {
            logger.error("Error finding user by DBID!", e);
            return Collections.emptyList();

        } catch (TimeoutException e) {
            logger.error("Timed out finding user by DBID! Is the connection overloaded?");
            return Collections.emptyList();
        }

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
                .command(QueryCommands.SERVER_GROUP.SERVERGROUP_GET_BYCLIENT)
                .addKey(PropertyKeys.Client.DBID_S, user.getClientDBID())
                .build();

        IQueryEvent.IAnswer answer;
        try {
            answer = server.getConnection().promiseRequest(sgDiscoverRequest).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted getting server groups from client.", e);
            Thread.currentThread().interrupt();
            return;

        } catch (ExecutionException e) {
            logger.error("Error getting server groups from client!", e);
            return;

        } catch (TimeoutException e) {
            logger.error("Timed out getting server groups from client! Is the connection overloaded?");
            return;
        }

        String groups = answer.getDataChain()
                .stream()
                .map(holder -> holder.getProperty("sgid"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(","));
        user.setProperty(PropertyKeys.Client.GROUPS, groups);
    }

    @Override
    public List<IUser> findUserByNickname(String ts3nickname) {
        if (ts3nickname == null || ts3nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname to search for may not be null, blank or empty!");
        }

        List<IClient> onlineClients = findClientByNickname(ts3nickname);
        if (!onlineClients.isEmpty()) {
            return new LinkedList<>(onlineClients);
        }

        IQueryRequest request = IQueryRequest.builder()
                .command(QueryCommands.CLIENT.CLIENT_FIND_DB)
                .addKey("pattern", ts3nickname)
                .build();

        IQueryEvent.IAnswer answer;
        try {
            answer = server.getConnection().promiseRequest(request).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted finding user by nickname.", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();

        } catch (ExecutionException e) {
            logger.error("Error finding user by nickname!", e);
            return Collections.emptyList();

        } catch (TimeoutException e) {
            logger.error("Timed out finding user by nickname! Is the connection overloaded?");
            return Collections.emptyList();
        }

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
