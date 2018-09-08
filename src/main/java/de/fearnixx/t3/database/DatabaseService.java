package de.fearnixx.t3.database;

import de.fearnixx.t3.event.bot.IBotStateEvent;
import de.fearnixx.t3.plugin.persistent.PluginManager;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.reflect.Listener;
import de.mlessmann.logging.ILogReceiver;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by MarkL4YG on 09-Feb-18
 */
public class DatabaseService {

    private static final Object CLASS_LOCK =  new Object();
    private static final List<Class<?>> ENTITIES = new CopyOnWriteArrayList<>();

    @Inject
    public ILogReceiver logger;

    @Inject
    public PluginManager pluginManager;

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
                applyDefaults(registryBuilder);
                registryBuilder.loadProperties(prop);

                persistenceUnits.put(name, new PersistenceUnitRep(registryBuilder.build(), getClasses()));
            }
        }
    }

    private void applyDefaults(StandardServiceRegistryBuilder registryBuilder) {
        registryBuilder.applySetting("hibernate.format_sql", "true");
        registryBuilder.applySetting("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        registryBuilder.applySetting("hibernate.connection.pool_size", "1");
        registryBuilder.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    }

    @Listener
    public void onShutdown(IBotStateEvent.IPostShutdown event) {
        persistenceUnits.forEach((k, u) -> u.close());
        persistenceUnits.clear();
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
            if (ENTITIES.isEmpty()) {
                logger.fine("Searching Entities.");

                Reflections reflect = pluginManager.getReflectionsWithUrls(pluginManager.getPluginUrls().toArray(new URL[0]));
                ENTITIES.addAll(reflect.getTypesAnnotatedWith(Entity.class));
            }
        }
    }

    private Set<Class<?>> getClasses() {
        synchronized (CLASS_LOCK) {
            return new HashSet<>(ENTITIES);
        }
    }
}
