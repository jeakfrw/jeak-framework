package de.fearnixx.jeak.service.database;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Function;

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
     *
     * @apiNote Connections have to be closed in order to be returned to the connection pool. Not closing them will leak connections and drain the pool.
     */
    DataSource getDataSource();

    /**
     * Opens the door to the Hibernate JPA implementation.
     * Classes annotated with {@link javax.persistence.Entity} are automatically registered with Hibernate.
     * @apiNote EntityManager implements {@link AutoCloseable} and should be closed when information processing is done. For example, when an event listener is finished. (Use try-with-resources, when possible.)
     */
    EntityManager getEntityManager();

    /**
     * Executes the given function with an newly created entity manager.
     * Also wraps the consumer into a transaction, which will be rolled back, if any exception occurs.
     * <p>
     * Nested calls of this function are explicitly permitted on the <b>same thread</b>.
     * In such cases the inner function call will use the same transaction and entity manager.
     * Subsequent, non-nested calls will receive own entity managers and transactions.
     *
     * @param entityManagerFunction the function which will be run
     */
    <T> T withEntityManager(Function<EntityManager, T> entityManagerFunction);

    /**
     * Just like {@link #withEntityManager(Function)} but with an error callback.
     *
     * @param entityManagerFunction the function which will be run
     * @param onError               the callback which will be run, if an exceptions occurs during the execution
     * @implNote When <i>onError</i> is called, the transaction has already been rolled back.
     */
    <T> T withEntityManager(Function<EntityManager, T> entityManagerFunction, Consumer<Exception> onError);
}
