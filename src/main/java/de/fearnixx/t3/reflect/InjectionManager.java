package de.fearnixx.t3.reflect;

import de.fearnixx.t3.Main;
import de.fearnixx.t3.database.DatabaseService;
import de.fearnixx.t3.service.IServiceManager;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

import static de.fearnixx.t3.T3Bot.CHAR_ENCODING;

/**
 * Created by MarkL4YG on 02-Feb-18
 */
public class InjectionManager implements IInjectionService {

    public static final Boolean UNIT_FULLY_QUALIFIED = Main.getProperty("bot.inject.fqunit", Boolean.FALSE);

    private static final Logger logger = LoggerFactory.getLogger(InjectionManager.class);

    private IServiceManager serviceManager;

    private String unitName;

    private File baseDir;

    public InjectionManager(IServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public <T> T injectInto(T victim) {
        return injectInto(victim, null);
    }

    public <T> T injectInto(T victim, String unitName) {
            // Logging
            logger.debug("Running injections on object of class: ", victim.getClass());

            Class<?> clazz = victim.getClass();
            Field[] fields = clazz.getFields();

            for (Field field : fields) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject == null)
                    continue;

                // Maybe a config field
                Config config = field.getAnnotation(Config.class);

                // Maybe a datasource field
                DataSource dataSource = field.getAnnotation(DataSource.class);

                // Field information
                Class<?> type = field.getType();
                String fieldName = field.getName();

                // Evaluate value
                Optional<?> optTarget;
                if (config != null)
                    optTarget = provideConfigWith(clazz, type, unitName, fieldName, config);
                else if (dataSource != null)
                    optTarget = provideDataSourceWith(clazz, type, unitName, fieldName, dataSource);
                else
                    optTarget = provideWith(clazz, type, unitName, fieldName);

                // Log message is provided by #provide
                if (!optTarget.isPresent())
                    continue;

                // Access state
                boolean accessState = field.isAccessible();
                field.setAccessible(true);

                try {
                    field.set(victim, optTarget.get());
                    logger.debug("Injected {} as {}", type.getCanonicalName(), field.getName());
                } catch (IllegalAccessException e) {
                    logger.error("Failed injection of class {} into object of class {}", type, clazz.toString(), e);
                }

                // Reset access state
                field.setAccessible(accessState);
            }
            return victim;
    }

    public <T> Optional<T> provide(Class<T> clazz) {
        Optional<T> svcResult = serviceManager.provide(clazz);
        if (svcResult.isPresent()) {
            return svcResult;
        }
        logger.warn("Failed to provide injection for: {}", clazz.toString());
        return Optional.empty();
    }

    public <T> Optional<T> provideWith(Class<?> victimClazz, Class<T> clazz, String altUnitName, String fieldName) {
        Optional<T> result = serviceManager.provide(clazz);
        if (result.isPresent())
            return result;

        InjectionManager value = null;
        String unitName = this.unitName != null ? this.unitName : altUnitName;
        if (!UNIT_FULLY_QUALIFIED) {
            if (unitName != null && unitName.contains(".")) {
                unitName = unitName.substring(unitName.lastIndexOf('.') + 1, unitName.length());
            }
        }

        if (clazz.isAssignableFrom(IInjectionService.class)) {
            value = new InjectionManager(serviceManager);
            value.setBaseDir(baseDir);
            value.setUnitName(unitName);

        }
        return Optional.ofNullable(clazz.cast(value));
    }

    public <T> Optional<T> provideConfigWith(Class<?> victimClazz, Class<T> clazz, String altUnitName, String fieldName, Config annotation) {
        Object value = null;
        String unitName = this.unitName != null ? this.unitName : altUnitName;

        String fileName = annotation.id();
        if (fileName.isEmpty())
            fileName = unitName;

        if (fileName == null) {
            fileName = victimClazz.getName() + '.' + fieldName;
            fileName = fileName.replaceAll("(?i)loader", "");
            fileName = fileName.replaceAll("(?i)config", "");
        }

        File baseDir = new File(this.baseDir, "config");
        if (unitName != null)
            baseDir = new File(baseDir, unitName);

        if (!annotation.category().isEmpty())
            baseDir = new File(baseDir, annotation.category());

        if (!baseDir.isDirectory() && !baseDir.mkdirs())
            throw new RuntimeException("Failed to create target directory: " + baseDir.getPath());

        File configFile = new File(baseDir, fileName + ".json");

        if (clazz.isAssignableFrom(ConfigLoader.class)) {
            value = new JSONConfigLoader();
            ((JSONConfigLoader) value).setEncoding(CHAR_ENCODING);
            ((JSONConfigLoader) value).setFile(configFile);

        } else if (clazz.isAssignableFrom(File.class)) {
            value = configFile;

        } else if (clazz.isAssignableFrom(Path.class)) {
            value = configFile.toPath();

        }

        return Optional.ofNullable(clazz.cast(value));
    }

    public <T> Optional<T> provideDataSourceWith(Class<?> victimClass, Class<T> clazz, String altUnitName, String fieldName, DataSource annotation) {
        if (annotation.value().isEmpty()) {
            throw new IllegalArgumentException("Cannot inject EntityManager without unit ID!");
        }

        DatabaseService service = serviceManager.provideUnchecked(DatabaseService.class);
        Optional<EntityManager> manager = service.getEntityManager(annotation.value());
        Object value = null;

        if (clazz.isAssignableFrom(EntityManager.class)) {
            if (!manager.isPresent()) {
                logger.warn("PersistenceInjection failed", new IllegalStateException("Failed to find persistence unit: " + annotation.value()));
                return Optional.empty();
            }
            value = manager.get();

        } else if (clazz.isAssignableFrom(Boolean.class)) {
            value = manager.isPresent();

        }

        return Optional.ofNullable(clazz.cast(value));
    }
}
