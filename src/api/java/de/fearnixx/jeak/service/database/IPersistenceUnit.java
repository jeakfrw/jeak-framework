package de.fearnixx.jeak.service.database;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

/**
 * Representation of a Hibernate-provided data source/persistence unit.
 * While sessions created by this unit are tracked and will be cleaned up, any sessions not directly retrieved from
 * this interface must be cleaned up by hand!
 */
public interface IPersistenceUnit {

    String getUnitId();

    DataSource getDataSource();

    EntityManager getEntityManager();
}
