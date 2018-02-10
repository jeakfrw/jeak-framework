package de.fearnixx.t3.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.EntityManager;
import javax.persistence.SynchronizationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by MarkL4YG on 09-Feb-18
 */
public class PersistenceUnitRep {
    private ServiceRegistry registry;
    private MetadataSources metaSources;
    private MetadataBuilder metaBuilder;
    private Metadata metaData;
    private SessionFactory sessionFactory;

    private List<EntityManager> managerList;

    public PersistenceUnitRep(ServiceRegistry registry, Set<Class<?>> classes) {
        this.registry = registry;
        this.metaSources = new MetadataSources(registry);
        for (Class<?> aClass : classes) {
            metaSources.addAnnotatedClass(aClass);
        }
        this.metaBuilder = metaSources.getMetadataBuilder();
        this.metaData = metaBuilder.build();
        this.sessionFactory = metaData.buildSessionFactory();
        this.managerList = new ArrayList<>(10);
    }

    public EntityManager getEntityManager() {
        EntityManager manager = sessionFactory.createEntityManager();
        managerList.add(manager);
        return manager;
    }

    public void close() {
        managerList.forEach(EntityManager::close);
        sessionFactory.close();
        managerList.clear();
        StandardServiceRegistryBuilder.destroy(registry);
    }
}
