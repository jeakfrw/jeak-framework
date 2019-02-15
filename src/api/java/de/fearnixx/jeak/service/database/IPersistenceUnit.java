package de.fearnixx.jeak.service.database;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

/**
 * Representation of a Hibernate-provided data source/persistence unit.
 * While sessions created by this unit are tracked and will be cleaned up, any sessions not directly retrieved from
 * this interface must be cleaned up manually!
 */
public interface IPersistenceUnit {

    /**
     * The persistence unit identifier.
     */
    String getUnitId();

    /**
     * Constructed (or configured) JDBC connection url.
     * "jdbc:$driver$://$host$:$port$/$schemaName§"
     */
    String getJdbcUrl();

    /**
     * Configured hostname.
     * Default: localhost
     */
    String getHost();

    /**
     * Configured server port.
     * Default: 3306
     */
    String getPort();

    /**
     * Name of the selected database.
     * Default: jeakbot
     */
    String getSchemaName();

    /**
     * Username for the connection.
     * Default: jeakbot-user
     */
    String getUsername();

    /**
     * Password for the connection.
     * Default: secret
     */
    String getPassword();

    /**
     * Used JDBC driver.
     * Default: mariadb
     */
    String getDriver();

    /**
     * Returns the underlying JPA {@link DataSource}.
     * You can use this to open connections for your own use.
     */
    DataSource getDataSource();

    /**
     * Opens the door to the Hibernate JPA implementation.
     * Classes annotated with {@link javax.persistence.Entity} are automatically registered with Hibernate.
     *
     * @implNote The method returns a new entity manager every time!
     */
    EntityManager getEntityManager();
}
