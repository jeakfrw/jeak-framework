package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.teamspeak.data.TS3User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@FrameworkService(serviceInterface = IUserService.class)
public class DBUserService extends AbstractUserService {

    private static final Logger logger = LoggerFactory.getLogger(DBUserService.class);

    private final IPersistenceUnit persistenceUnit;

    @Inject
    private IServer server;

    @Inject
    private IDataCache dataCache;

    public DBUserService(IPersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    @Override
    public List<IUser> findUserByUniqueID(String ts3uniqueID) {
        if (ts3uniqueID == null || ts3uniqueID.isBlank()) {
            throw new IllegalArgumentException("TS3 unique ID may not be null, blank or empty!");
        }

        List<TS3User> results = new LinkedList<>();
        withConnection(conn -> {
            String query = "SELECT * FROM clients c WHERE c.client_unique_id = ? AND c.server_id = ?";
            getUsersFromDB(results, conn, query, ts3uniqueID);
            populateOrRemoveUsers(results, conn);
        });
        return new ArrayList<>(results);
    }

    @Override
    public List<IUser> findUserByDBID(int ts3dbID) {
        List<TS3User> results = new LinkedList<>();
        withConnection(conn -> {
            String query = "SELECT * FROM clients c WHERE c.client_id = ? AND c.server_id = ?";
            getUsersFromDB(results, conn, query, Integer.toString(ts3dbID));
            populateOrRemoveUsers(results, conn);
        });
        return new ArrayList<>(results);
    }

    @Override
    public List<IUser> findUserByNickname(String ts3nickname) {
        if (ts3nickname == null || ts3nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname to search for may not be null, blank or empty!");
        }

        List<TS3User> results = new LinkedList<>();
        withConnection(conn -> {
            String query = "SELECT * FROM clients c WHERE c.client_nickname LIKE %?% AND c.server_id = ?";
            getUsersFromDB(results, conn, query, ts3nickname);
            populateOrRemoveUsers(results, conn);
        });
        return new ArrayList<>(results);
    }

    private void getUsersFromDB(List<TS3User> results, Connection conn, String query, String search) {
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, search);
            statement.setInt(2, server.getInstanceId());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.isBeforeFirst()) {
                    logger.warn("Failed to get client for search: {} - no entry present.", search);
                }

                while (result.next()) {
                    TS3User user = new TS3User();
                    user.setProperty(PropertyKeys.Client.DBID, result.getInt("client_id"));
                    user.setProperty(PropertyKeys.Client.UID, result.getString("client_unique_id"));
                    user.setProperty(PropertyKeys.Client.NICKNAME, result.getString("client_nickname"));
                    user.setProperty(PropertyKeys.Client.LAST_JOIN_TIME, result.getLong("client_lastconnected"));
                    user.setProperty(PropertyKeys.DBClient.TOTAL_CONNECTIONS, result.getInt(PropertyKeys.DBClient.TOTAL_CONNECTIONS));
                    user.setProperty(PropertyKeys.Client.IPV4_ADDRESS, result.getString("client_lastip"));
                    applyPermissions(user);

                    results.add(user);
                    logger.debug("Constructed user: {}/{}", user.getClientDBID(), user.getNickName());
                }
            }
        } catch (SQLException e) {
            results.clear();
            logger.error("Failed to construct results from SQL result set!", e);
        }
    }

    private void populateOrRemoveUsers(List<TS3User> results, Connection connection) {
        results.removeIf(user -> {
            String query = "SELECT prop.ident, prop.value FROM client_properties prop WHERE prop.id = ? AND prop.server_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, user.getClientDBID());
                statement.setInt(2, server.getInstanceId());

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.isBeforeFirst()) {
                        logger.warn("No client properties found for user: {}", user);
                        // This user could not be populated - remove it!
                        return true;
                    } else {
                        logger.trace("Populating user properties for: {}", user);
                    }

                    while (result.next()) {
                        String key = result.getString("ident");
                        String value = result.getString("value");
                        user.setProperty(key, value);
                        logger.trace("Adding client property: {} -> {}", key, value);
                    }
                }

                // Properties read - everything's fine.
            } catch (SQLException e) {
                logger.error("Failed to get client properties for user: {}", user, e);
                // This user could not be populated - remove it!
                return true;
            }

            // Try reading server groups
            String permQuery = "SELECT sg.group_id FROM group_server_to_client sg WHERE sg.server_id = ? AND sg.id1 = ?";
            try (PreparedStatement statement = connection.prepareStatement(permQuery)) {
                statement.setInt(1, server.getInstanceId());
                statement.setInt(2, user.getClientDBID());
                Set<String> groups = new HashSet<>();
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        groups.add(result.getString("group_id"));
                    }
                }
                String serverGroups = groups.stream().collect(Collectors.joining(","));
                user.setProperty(PropertyKeys.Client.GROUPS, serverGroups);

            } catch (SQLException e) {
                logger.error("Failed to get server groups for user: {}", user, e);
                return true;
            }
            return false;
        });
    }

    private synchronized void withConnection(Consumer<Connection> consumer) {
        try (Connection conn = persistenceUnit.getDataSource().getConnection()) {
            consumer.accept(conn);
        } catch (SQLException e) {
            logger.error("Failed to get database connection!", e);
        }
    }
}
