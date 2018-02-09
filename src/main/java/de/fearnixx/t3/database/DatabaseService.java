package de.fearnixx.t3.database;

import de.fearnixx.t3.event.bot.IBotStateEvent;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.reflect.Listener;
import de.mlessmann.config.ConfigNode;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MarkL4YG on 09-Feb-18
 */
public class DatabaseService {

    private static final Object CLASS_LOCK =  new Object();
    private static Set<Class<?>> ENTITIES;

    @Inject(id = "DBSVC")
    public ILogReceiver logger;

    private File dbDir;

    private Map<String, PersistenceUnitRep> persistenceUnits;

    public DatabaseService(File dbDir) {
        this.dbDir = dbDir;
        persistenceUnits = new ConcurrentHashMap<>();
    }

    private List<File> getPropertyFiles() {
        List<File> list = new ArrayList<>();
        if (dbDir.isDirectory()) {
            File[] files = dbDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.canRead() && file.getName().endsWith(".properties")) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    public void onLoad() {
        List<File> properties = getPropertyFiles();

        if (!properties.isEmpty()) {
            logger.info("At least one persistence unit has been found.");
            checkClasses();

            for (File prop : properties) {
                String name = prop.getName().substring(0, prop.getName().length() - 11);
                logger.fine("Constructing persistence unit: ", name);

                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
                registryBuilder.loadProperties(prop);
                registryBuilder.applySetting("hibernate.format_sql", "true");
                //jndi
                //registryBuilder.applySetting("hibernate.session_factory_name", name);
                //registryBuilder.configure();

                persistenceUnits.put(name, new PersistenceUnitRep(registryBuilder.build(), getClasses()));
            }
        }
    }

    @Listener
    public void onShutdown(IBotStateEvent.IPreShutdown event) {

    }

    public Optional<EntityManager> getEntityManager(String unitName) {
        PersistenceUnitRep rep = persistenceUnits.getOrDefault(unitName, null);
        if (rep != null) {
            return Optional.of(rep.getEntityManager());
        }
        return Optional.empty();
    }

    private void checkClasses() {
        synchronized (CLASS_LOCK) {
            if (ENTITIES == null) {
                logger.fine("Searching Entities.");

                Reflections reflect = new Reflections();
                ENTITIES = reflect.getTypesAnnotatedWith(Entity.class);
            }
        }
    }

    private Set<Class<?>> getClasses() {
        synchronized (CLASS_LOCK) {
            return ENTITIES;
        }
    }
}
