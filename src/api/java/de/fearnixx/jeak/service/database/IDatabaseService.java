package de.fearnixx.jeak.service.database;

import java.util.Optional;

public interface IDatabaseService {

    Optional<IPersistenceUnit> getPersistenceUnit(String unitId);
}
