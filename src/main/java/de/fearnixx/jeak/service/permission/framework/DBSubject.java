package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class DBSubject extends PermissionSubject {

    private static final Logger logger = LoggerFactory.getLogger(DBSubject.class);

    private DataSource dataSource;

    public DBSubject(UUID subjectUUID, DataSource dataSource) {
        super(subjectUUID);
        this.dataSource = dataSource;
    }

    @Override
    public Optional<IPermission> getPermission(String permSID) {
        String query = "SELECT perm_value FROM permissions WHERE subject_uid = ? AND perm_sid = ?";
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(query)) {
            statement.setString(1, getUniqueID().toString());
            statement.setString(2, permSID);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                int permValue = result.getInt(1);
                IPermission perm = new FrameworkPermission(getUniqueID(), permSID, permValue);
                return Optional.of(perm);
            }
        } catch (SQLException e) {
            logger.warn("Failed to get permission \"{}\" for subject: \"{}\"", permSID, getUniqueID(), e);
        }
        return Optional.empty();
    }

    @Override
    public SubjectType getType() {
        return SubjectType.USER;
    }
}
