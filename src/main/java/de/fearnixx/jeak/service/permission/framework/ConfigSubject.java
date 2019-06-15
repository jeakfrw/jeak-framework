package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.IValueHolder;

import javax.swing.plaf.SeparatorUI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigSubject extends StoredSubject {

    private final IConfig configRef;

    public ConfigSubject(UUID subjectUUID, IConfig configRef, Function<UUID, IGroup> groupSupplier) {
        super(subjectUUID, groupSupplier);
        this.configRef = configRef;
    }

    @Override
    public Optional<IPermission> getPermission(String permSID) {
        IConfigNode valueNode = configRef.getRoot()
                .getNode("permissions", permSID, "value");

        if (valueNode.isPrimitive()) {
            Integer permValue = valueNode.asInteger();
            FrameworkPermission perm = new FrameworkPermission(permSID, permValue);
            return Optional.of(perm);
        }

        return Optional.empty();
    }

    @Override
    public List<IGroup> getParents() {
        return configRef.getRoot()
                .getNode("parents")
                .optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IValueHolder::asString)
                .map(UUID::fromString)
                .map(this::makeGroup)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(String permission) {
        return;
    }
}
