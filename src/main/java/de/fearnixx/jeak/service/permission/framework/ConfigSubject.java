package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;

import javax.swing.plaf.SeparatorUI;
import java.util.Optional;
import java.util.UUID;

public class ConfigSubject extends PermissionSubject {

    private final IConfig configRef;

    public ConfigSubject(UUID subjectUUID, IConfig configRef) {
        super(subjectUUID);
        this.configRef = configRef;
    }

    @Override
    public Optional<IPermission> getPermission(String permSID) {
        IConfigNode valueNode = configRef.getRoot()
                .getNode("permissions", permSID, "value");

        if (valueNode.isPrimitive()) {
            Integer permValue = valueNode.asInteger();
            FrameworkPermission perm = new FrameworkPermission(getUniqueID(), permSID, permValue);
            return Optional.of(perm);
        }

        return Optional.empty();
    }

    @Override
    public SubjectType getType() {
        return SubjectType.USER;
    }
}
