package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.ISubject;

import java.util.UUID;
import java.util.function.Function;

public abstract class StoredSubject implements ISubject {

    private final UUID subjectUUID;
    private final Function<UUID, IGroup> groupSupplier;

    public StoredSubject(UUID subjectUUID, Function<UUID, IGroup> groupSupplier) {
        this.subjectUUID = subjectUUID;
        this.groupSupplier = groupSupplier;
    }

    @Override
    public UUID getUniqueID() {
        return subjectUUID;
    }

    protected IGroup makeGroup(UUID uuid) {
        return groupSupplier.apply(uuid);
    }

}
