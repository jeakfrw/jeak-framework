package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceUnit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DBPermissionProvider extends AbstractTS3PermissionProvider {

    private static final Logger logger = LoggerFactory.getLogger(DBPermissionProvider.class);

    @Inject
    @PersistenceUnit(name = "ts3perms")
    private IPersistenceUnit persistenceUnit;

    @Override
    public void clearCache(ITS3Permission.PriorityType type, Integer optClientOrGroupID, Integer optChannelID) {
        // There is no caching in the database connected service.
    }

    @Override
    public Optional<ITS3Permission> getClientPermission(Integer clientDBID, String permSID) {
        return selectFromTable(clientDBID, permSID, "perm_client", null);
    }

    @Override
    public Optional<ITS3Permission> getServerGroupPermission(Integer serverGroupID, String permSID) {
        return selectFromTable(serverGroupID, permSID, "perm_server_group", null);
    }

    @Override
    public Optional<ITS3Permission> getChannelGroupPermission(Integer channelGroupID, String permSID) {
        return selectFromTable(channelGroupID, permSID, "perm_channel_groups", null);
    }

    @Override
    public Optional<ITS3Permission> getChannelClientPermission(Integer channelID, Integer clientDBID, String permSID) {
        return selectFromTable(channelID, permSID, "perm_channel_clients", clientDBID);
    }

    @Override
    public Optional<ITS3Permission> getChannelPermission(Integer channelID, String permSID) {
        return selectFromTable(channelID, permSID, "perm_channel", null);
    }

    private Optional<ITS3Permission> selectFromTable(Integer idOne, String permSID, String tableName, Integer idTwo) {
        String sql = "SELECT perm_value, perm_negated, perm_skip FROM " + tableName + " WHERE server_id = ? AND id1 = ?";

        if (idTwo != null) {
            sql += " AND id2 = ?";
        }

        try (PreparedStatement statement = persistenceUnit.getDataSource()
                .getConnection()
                .prepareStatement(sql)) {
            statement.setInt(1, getServerId());
            statement.setInt(2, idOne);
            if (idTwo != null) {
                statement.setInt(3, idTwo);
            }

            ResultSet res = statement.executeQuery();
            TS3Permission perm = new TS3Permission(ITS3Permission.PriorityType.CLIENT, permSID);
            if (res.isBeforeFirst()) {
                res.next();
                perm.setValue(res.getInt(1));
                perm.setNegated(res.getInt(2) == 1);
                perm.setSkipped(res.getInt(3) == 1);
            } else {
                perm.setValue(0);
                perm.setNegated(false);
                perm.setSkipped(false);
            }
            return Optional.of(perm);

        } catch (SQLException e) {
            logger.error("Failed to get permission value from database! {} for {}", permSID, idOne, e);
            return Optional.empty();
        }
    }
}
