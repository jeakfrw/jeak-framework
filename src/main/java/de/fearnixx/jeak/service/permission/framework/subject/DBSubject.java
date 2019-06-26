package de.fearnixx.jeak.service.permission.framework.subject;

import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.framework.FrameworkPermission;
import de.fearnixx.jeak.service.permission.framework.SubjectCache;
import de.fearnixx.jeak.service.permission.framework.membership.MembershipIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DBSubject extends SubjectAccessor {

    private static final Logger logger = LoggerFactory.getLogger(DBSubject.class);

    private DataSource dataSource;

    public DBSubject(UUID subjectUUID, SubjectCache permissionSvc, DataSource dataSource, MembershipIndex membershipIndex) {
        super(subjectUUID, permissionSvc, membershipIndex);
        this.dataSource = dataSource;
    }

    @Override
    protected Optional<IPermission> getPermissionFromSelf(String permSID) {
        String query = "SELECT perm_value FROM permissions WHERE subject_uid = ? AND perm_sid = ?";
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(query)) {
            statement.setString(1, getUniqueID().toString());
            statement.setString(2, permSID);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                int permValue = result.getInt(1);
                IPermission perm = new FrameworkPermission(permSID, permValue);
                return Optional.of(perm);
            }
        } catch (SQLException e) {
            logger.warn("Failed to get permission \"{}\" for subject: \"{}\"", permSID, getUniqueID(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<IPermission> getPermissions() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Optional<Integer> getLinkedServerGroup() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public boolean hasPermission(String permission) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public boolean setPermission(String permission, int value) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public boolean removePermission(String permission) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void saveIfModified() {
        // not needed for database operations.
    }

    @Override
    public void mergeFrom(SubjectAccessor from) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void invalidate() {
        // not needed for database operations.
        // May be implemented for cleanup purposes.
    }
}
