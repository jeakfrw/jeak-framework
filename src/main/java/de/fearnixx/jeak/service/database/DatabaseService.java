package de.fearnixx.jeak.service.database;

import de.fearnixx.jeak.event.bot.BotStateEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.plugin.persistent.PluginManager;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by MarkL4YG on 09-Feb-18
 */
@FrameworkService(serviceInterface = IDatabaseService.class)
public class DatabaseService implements IDatabaseService {

    private static final Object CLASS_LOCK = new Object();
    private static final List<Class<?>> ENTITIES = new CopyOnWriteArrayList<>();

    public static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Inject
    public PluginManager pluginManager;

    private final File dbDir;

    private BootstrapServiceRegistry baseRegistry;
    private final Map<String, HHPersistenceUnit> persistenceUnits = new ConcurrentHashMap<>();

    public DatabaseService(File dbDir) {
        this.dbDir = dbDir;
    }

    private List<File> getDatabaseConfigurations() {
        List<File> list = new ArrayList<>();
        if (dbDir.isDirectory()) {
            File[] files = dbDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    //@Listener(order = Listener.Orders.SYSTEM)
    public void onLoad(BotStateEvent.PreInitializeEvent event) {
        List<File> dataSourceFiles = getDatabaseConfigurations();

        if (!dataSourceFiles.isEmpty()) {
            checkClasses();

            BootstrapServiceRegistryBuilder baseRegistryBuilder = new BootstrapServiceRegistryBuilder();
            baseRegistryBuilder.applyClassLoader(pluginManager.getPluginClassLoader());
            this.baseRegistry = baseRegistryBuilder.build();

            for (File dataSourceFile : dataSourceFiles) {
                createUnitFromFile(dataSourceFile);
            }
        }
    }

    private void createUnitFromFile(File dataSourceFile) {
        String name = dataSourceFile.getName().substring(0, dataSourceFile.getName().length() - 5);
        logger.info("Constructing persistence unit: {}", name);

        IConfigLoader loader = LoaderFactory.getLoader("application/json");
        HHPersistenceUnit unit = new HHPersistenceUnit(
                name,
                new FileConfig(loader, dataSourceFile),
                baseRegistry
        );

        if (unit.initialize()) {
            persistenceUnits.put(name, unit);
        }
    }

    private void checkClasses() {
        synchronized (CLASS_LOCK) {
            if (ENTITIES.isEmpty()) {
                logger.debug("Searching Entities.");

                final var scanner = pluginManager.getPluginScanner();
                try (final var result = scanner.scan()) {
                    List<Class<?>> types = result.getClassesWithAnnotation(Entity.class.getName()).loadClasses(true);
                    types.forEach(entityType -> {
                        logger.debug("Found: {}", entityType.getName());
                        ENTITIES.add(entityType);
                    });
                }
            }
        }
    }

    public static Set<Class<?>> getClasses() {
        synchronized (CLASS_LOCK) {
            return new HashSet<>(ENTITIES);
        }
    }

    @Listener(order = Listener.Orders.LATEST)
    public void onShutdown(IBotStateEvent.IPostShutdown event) {
        persistenceUnits.forEach((k, unit) -> {
            try {
                unit.close();
            } catch (Exception e) {
                logger.warn("Failed to close persistence unit: {}", k, e);
            }
        });
        persistenceUnits.clear();
        BootstrapServiceRegistryBuilder.destroy(baseRegistry);
    }

    public Optional<IPersistenceUnit> getPersistenceUnit(String unitName) {
        IPersistenceUnit rep = persistenceUnits.getOrDefault(unitName, null);
        if (rep != null) {
            return Optional.of(rep);
        }
        return Optional.empty();
    }
}
