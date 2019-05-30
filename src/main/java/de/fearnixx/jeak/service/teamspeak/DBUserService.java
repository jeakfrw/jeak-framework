package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.teamspeak.data.TS3User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@FrameworkService(serviceInterface = IUserService.class)
public class DBUserService extends AbstractUserService {

    private static final Logger logger = LoggerFactory.getLogger(DBUserService.class);

    private IPersistenceUnit persistenceUnit;

    @Inject
    private IServer server;

    @Inject
    private IDataCache dataCache;

    public DBUserService(IPersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    @Override
    public List<IUser> findUserByUniqueID(String ts3uniqueID) {
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
            getUsersFromDB(results, conn, query, ts3dbID);
        });
        return new ArrayList<>(results);
    }

    @Override
    public List<IUser> findUserByNickname(String ts3nickname) {
        List<TS3User> results = new LinkedList<>();
        withConnection(conn -> {
            String query = "SELECT * FROM clients c WHERE c.client_nickname LIKE ? AND c.server_id = ?";
            getUsersFromDB(results, conn, query, "%" + ts3nickname + "%");
        });
        return new ArrayList<>(results);
    }

    private void getUsersFromDB(List<TS3User> results, Connection conn, String query, Object search) {
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setObject(1, search);
            statement.setInt(2, server.getInstanceId());
            ResultSet result = statement.executeQuery();

            if (!result.isBeforeFirst()) {
                logger.warn("Failed to get client for search: {} - no entry present.", search);
            }

            while (result.next()) {
                TS3User user = new TS3User();
                user.setProperty(PropertyKeys.Client.ID, result.getInt("client_id"));
                user.setProperty(PropertyKeys.Client.UID, result.getString("client_unique_id"));
                user.setProperty(PropertyKeys.Client.NICKNAME, result.getString("client_nickname"));
                user.setProperty(PropertyKeys.Client.LAST_JOIN_TIME, result.getLong("client_lastconnected"));
                user.setProperty(PropertyKeys.DBClient.TOTAL_CONNECTIONS, result.getInt(PropertyKeys.DBClient.TOTAL_CONNECTIONS));
                user.setProperty(PropertyKeys.Client.IPV4_ADDRESS, result.getString("client_lastip"));
                results.add(user);
                logger.debug("Constructed user: {}/{}", user.getClientDBID(), user.getNickName());
            }
        } catch (SQLException e) {
            results.clear();
            logger.error("Failed to construct results from SQL result set!", e);
        }
    }

    private void populateOrRemoveUsers(List<TS3User> results, Connection connection) {
        results.removeIf(user -> {
            Integer dbId = user.getClientDBID();
            String query = "SELECT prop.ident, prop.value FROM client_properties prop WHERE prop.id = ? AND prop.server_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, dbId);
                statement.setInt(2, server.getInstanceId());
                ResultSet result = statement.executeQuery();

                if (!result.isBeforeFirst()) {
                    logger.warn("No client properties found for user: {}/{}", user.getClientDBID(), user.getNickName());
                    // This user could not be populated - remove it!
                    return true;
                }

                while (result.next()) {
                    String key = result.getString("ident");
                    String value = result.getString("value");
                    user.setProperty(key, value);
                    logger.debug("Adding client property: {}={}", key, value);
                }

                // Properties read - everything's fine.
                return false;
            } catch (SQLException e) {
                logger.error("Failed to get client properties for user: {}/{}", user.getClientDBID(), user.getNickName());
                // This user could not be populated - remove it!
                return true;
            }
        });
    }

    @Override
    public List<IClient> findClientByUniqueID(String ts3uniqueID) {
        return null;
    }

    @Override
    public List<IClient> findClientByDBID(int ts3dbID) {
        return null;
    }

    @Override
    public List<IClient> findClientByNickname(String ts3nickname) {
        return null;
    }

    @Override
    public Optional<IClient> getClientByID(int clientId) {
        return Optional.empty();
    }

    private synchronized void withConnection(Consumer<Connection> consumer) {
        try {
            consumer.accept(persistenceUnit.getDataSource().getConnection());
        } catch (SQLException e) {
            logger.error("Failed to get database connection!", e);
        }
    }
}
