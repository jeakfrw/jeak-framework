package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.service.permission.base.IPermission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subjects are "things" that can have or lack permissions.
 * At the moment, these are users (and thus, clients) and groups.
 */
public interface Subject {

    UUID getUniqueID();

    List<Subject> getParents();

    boolean hasPermission(String permission);

    Optional<IPermission> getPermission(String permission);
}
