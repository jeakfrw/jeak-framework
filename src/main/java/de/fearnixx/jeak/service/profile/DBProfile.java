package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

@SuppressWarnings("squid:S2095")
public abstract class DBProfile implements IUserProfile {

    private static final Logger logger = LoggerFactory.getLogger(DBProfile.class);

    private final UUID uuid;
    private final Connection userProfileConnection;

    public DBProfile(UUID uuid, Connection userProfileConnection) {
        this.uuid = uuid;
        this.userProfileConnection = userProfileConnection;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public List<IUserIdentity> getTSIdentities() {
        return getLinkedIdentities(IUserIdentity.SERVICE_TEAMSPEAK);
    }

    @Override
    public List<IUserIdentity> getLinkedIdentities(String serviceId) {
        synchronized (userProfileConnection) {
            List<IUserIdentity> results = new LinkedList<>();
            String query = "SELECT identity FROM frw_profiles_identities WHERE uuid = ? AND serviceId = ?";
            try (PreparedStatement statement = userProfileConnection.prepareStatement(query)) {
                statement.setString(1, getUniqueId().toString());
                statement.setString(2, serviceId);
                final ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    results.add(new UserIdentity(serviceId, resultSet.getString(1)));
                }

                return results;
            } catch (SQLException e) {
                logger.warn("Failed to look up identities for profile \"{}\" and service \"{}\"", uuid, serviceId);
                return Collections.emptyList();
            }
        }
    }

    @Override
    public List<IUserIdentity> getLinkedIdentities() {
        synchronized (userProfileConnection) {
            List<IUserIdentity> results = new LinkedList<>();
            String query = "SELECT serviceId, identity FROM frw_profiles_identities WHERE uuid = ?";
            try (PreparedStatement statement = userProfileConnection.prepareStatement(query)) {
                statement.setString(1, getUniqueId().toString());
                final ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    results.add(new UserIdentity(resultSet.getString(1), resultSet.getString(2)));
                }

                return results;
            } catch (SQLException e) {
                logger.warn("Failed to look up identities for profile \"{}\"", uuid);
                return Collections.emptyList();
            }
        }
    }

    @Override
    public Optional<String> getOption(String optionId) {
        synchronized (userProfileConnection) {
            String query = "SELECT value FROM frw_profiles_options WHERE ident = ? LIMIT 1";
            try (PreparedStatement statement = userProfileConnection.prepareStatement(query)) {
                statement.setString(1, optionId);
                final ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return Optional.of(resultSet.getString(1));
                }
            } catch (SQLException e) {
                logger.warn("Failed to retrieve option \"{}\" for profile \"{}\"", optionId, uuid, e);
            }
        }

        return Optional.empty();
    }

    @Override
    public String getOption(String optionId, String def) {
        return getOption(optionId).orElse(def);
    }

    @Override
    public void setOption(String optionId, String value) {
        synchronized (userProfileConnection) {
            String query = "INSERT into frw_profiles_options (`ident`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(`value`)";
            try (PreparedStatement statement = userProfileConnection.prepareStatement(query)) {
                statement.setString(1, optionId);

                if (value != null) {
                    statement.setString(2, value);
                } else {
                    statement.setNull(2, Types.VARCHAR);
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to set option \"{}\" for profile \"{}\"", optionId, uuid);
            }
        }
    }

    @Override
    public void removeOption(String optionId) {
        synchronized (userProfileConnection) {
            String query = "DELETE FROM frw_profiles_options WHERE `ident` = ?";
            try (PreparedStatement statement = userProfileConnection.prepareStatement(query)) {
                statement.setString(1, optionId);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to delete option \"{}\" from profile \"{}\"", optionId, uuid);
            }
        }
    }
}
