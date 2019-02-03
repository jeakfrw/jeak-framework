package de.fearnixx.jeak.service.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;

public class PersistenceUnitAccessor implements IPersistenceUnit, AutoCloseable {

    private boolean isClosed = false;
    private final SessionFactory sessionFactory;
    private final String persistenceUnitId;
    private final ServiceRegistry registry;

    // Lazily populated fields
    private DataSource dataSource = null;
    private EntityManager entityManager = null;

    public PersistenceUnitAccessor(String persistenceUnitId, ServiceRegistry registry, Collection<Class<?>> classes) {
        this.persistenceUnitId = persistenceUnitId;
        this.registry = registry;

        MetadataSources metaSources = new MetadataSources(registry);
        for (Class<?> aClass : classes) {
            metaSources.addAnnotatedClassName(aClass.getName());
        }
        sessionFactory = metaSources.getMetadataBuilder().build().buildSessionFactory();
    }

    @Override
    public String getUnitId() {
        return persistenceUnitId;
    }

    @Override
    public synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = sessionFactory.unwrap(DataSource.class);
        }

        return dataSource;
    }

    @Override
    public synchronized EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager= sessionFactory.createEntityManager();
        }

        return entityManager;
    }

    @Override
    public synchronized void close() throws Exception {
        if (isClosed) {
            throw new IOException("Persistence unit already closed!");
        }

        isClosed = true;
        sessionFactory.close();
        StandardServiceRegistryBuilder.destroy(registry);
    }
}
