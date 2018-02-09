package de.fearnixx.t3.reflect;

import de.fearnixx.t3.IBot;
import de.fearnixx.t3.Main;
import de.fearnixx.t3.service.IServiceManager;
import de.mlessmann.config.JSONConfigLoader;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.fearnixx.t3.T3Bot.CHAR_ENCODING;

/**
 * Created by MarkL4YG on 02-Feb-18
 */
public class InjectionManager implements IInjectionService {

    public static final Boolean UNIT_FULLY_QUALIFIED = Main.getProperty("bot.inject.fqunit", Boolean.FALSE);

    private ILogReceiver logger;
    private ILogReceiver loggerUnbiased;
    private IServiceManager serviceManager;

    private String unitName;

    private File baseDir;

    public InjectionManager(ILogReceiver logger, IServiceManager serviceManager) {
        this.logger = logger.getChild("INJ");
        this.loggerUnbiased = logger;
        this.serviceManager = serviceManager;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public void injectInto(Object victim) {
        injectInto(victim, null);
    }

    public void injectInto(Object victim, String unitName) {
            // Logging
            logger.finer("Running injections on object of class: ", victim.getClass());

            Class<?> clazz = victim.getClass();
            Field[] fields = clazz.getFields();

            for (Field field : fields) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject == null)
                    continue;

                // Extract info
                String id = inject.id();

                // Injection value
                Class<?> type = field.getType();
                Optional<?> optTarget = provideWith(type, id, unitName);

                // Log message is provided by #provide
                if (!optTarget.isPresent())
                    continue;

                // Access state
                boolean accessState = field.isAccessible();
                field.setAccessible(true);

                try {
                    field.set(victim, optTarget.get());
                    logger.finest("Injected ", type.getCanonicalName(), " as ", field.getName());
                } catch (IllegalAccessException e) {
                    logger.severe("Failed injection of class ", type.toString(), " into object of class ", clazz.toString(), e);
                }

                // Reset access state
                field.setAccessible(accessState);
            }
    }

    public <T> Optional<T> provide(Class<T> clazz) {
        Optional<T> svcResult = serviceManager.provide(clazz);
        if (svcResult.isPresent()) {
            return svcResult;
        }
        logger.warning("Failed to provide injection for: ", clazz.toString());
        return Optional.empty();
    }

    public <T> Optional<T> provideWith(Class<T> clazz, String id, String altUnitName) {
        Optional<T> result = serviceManager.provide(clazz);
        if (result.isPresent())
            return result;

        Object value = null;
        String unitName = this.unitName != null ? this.unitName : altUnitName;
        if (!UNIT_FULLY_QUALIFIED) {
            if (unitName != null && unitName.contains("."))
                unitName = unitName.substring(unitName.lastIndexOf('.') + 1, unitName.length());
        }

        if (clazz.isAssignableFrom(ILogReceiver.class)) {
            id = genID(id);
            value = loggerUnbiased;
            if (unitName != null)
                value = ((ILogReceiver) value).getChild(unitName);
            value = ((ILogReceiver) value).getChild(id);

        } else if (clazz.isAssignableFrom(ConfigLoader.class)) {
            File jsonFile;
            if (id == null || id.trim().isEmpty()) {
                jsonFile = new File(baseDir, "config/" + unitName + ".json");
            } else {
                File subDir = new File(baseDir, "config/" + unitName);
                subDir.mkdirs();
                jsonFile = new File(subDir, id + ".json");
            }
            value = new JSONConfigLoader();
            ((JSONConfigLoader) value).setEncoding(CHAR_ENCODING);
            ((JSONConfigLoader) value).setFile(jsonFile);

        } else if (clazz.isAssignableFrom(IInjectionService.class)) {
            value = new InjectionManager(loggerUnbiased, serviceManager);
            ((InjectionManager) value).setBaseDir(baseDir);
            ((InjectionManager) value).setUnitName(id != null ? id : unitName);

        }
        return Optional.ofNullable(clazz.cast(value));
    }

    private String genID(String id) {
        if (id != null && !id.trim().isEmpty()) {
            return id;
        } else {
            return UUID.randomUUID().toString();
        }
    }
}
