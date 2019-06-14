package de.fearnixx.jeak.service.permission.framework;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IGroup extends Subject {

    Optional<Integer> getLinkedServerGroup();

    List<UUID> getMemberSubjects();
}
