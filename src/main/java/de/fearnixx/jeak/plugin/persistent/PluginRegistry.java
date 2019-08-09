package de.fearnixx.jeak.plugin.persistent;

import de.fearnixx.jeak.event.IEvent;
import de.fearnixx.jeak.plugin.PluginContainer;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class PluginRegistry {

    private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

    public static Optional<PluginRegistry> getFor(Class<?> pluginClass) {
        PluginRegistry pr = new PluginRegistry(pluginClass);
        if (!pr.analyze()) {
            return Optional.empty();
        }
        return Optional.of(pr);
    }

    private final Class<?> pluginClass;
    private JeakBotPlugin tag;

    private String id;
    private String version;
    private String buildAgainst;
    private List<String> dependencies;

    private List<Method> listeners;
    private Map<Class<?>, List<Field>> injections;

    private PluginRegistry(Class<?> pluginClass) {
        this.pluginClass = pluginClass;
    }

    protected boolean analyze() {
        logger.debug("Analyzing class: {} from {}",
                pluginClass::toGenericString,
                () -> pluginClass.getProtectionDomain().getCodeSource().getLocation().getPath());

        tag = pluginClass.getAnnotation(JeakBotPlugin.class);
        if (tag == null) {
            logger.error("Attempt to analyze untagged plugin class: {}", pluginClass.toGenericString());
            return false;
        }
        id = tag.id();
        if (!id.matches("^[a-zA-Z.][a-zA-Z.]+$")) {
            logger.error("Plugin ID: {} is invalid!", this.id);
            return false;
        }
        version = Common.stripVersion(tag.version());
        if ("0".equals(version)) {
            logger.warn("Plugin ID: {} is using an invalid version: {}", this.id, tag.version());
            version = null;
        }

        // TODO: Bot version verification implementation
        buildAgainst = tag.builtAgainst();

        if (tag.depends().length == 0) {
            dependencies = Collections.emptyList();
        } else {
            dependencies = List.of(tag.depends());
        }


        logger.debug("Pre-processing listeners");
        listeners = new ArrayList<>();

        Method[] methods = pluginClass.getDeclaredMethods();
        for (Method method : methods) {
            Annotation anno = method.getAnnotation(Listener.class);
            if (anno == null) continue;
            if (method.getParameterCount() != 1) {
                logger.debug("Wrong parameter count for method: {}", method.getName());
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                logger.debug("Wrong visibility for method: {}", method.getName());
                continue;
            }
            if (!IEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                logger.debug("Wrong parameterType for method: {}", method.getName());
                continue;
            }

            listeners.add(method);
        }

        logger.debug("Pre-processing injections");
        injections = new HashMap<>();
        Field[] fields = pluginClass.getFields();
        for (Field field : fields) {
            if (field.getAnnotation(Inject.class) == null) continue;
            int mod = field.getModifiers();
            if (!Modifier.isPublic(mod)
                    || Modifier.isAbstract(mod)
                    || Modifier.isFinal(mod)
                    || Modifier.isVolatile(mod)) {
                logger.debug("Wrong modifiers for field: {}", field.getName());
            }
            List<Field> l = injections.getOrDefault(field.getType(), null);
            if (l == null) {
                l = new ArrayList<>();
                injections.put(field.getType(), l);
            }
            l.add(field);
        }

        logger.debug("Plugin class {} analysed", pluginClass.toGenericString());
        final String logMessage = String.format("ID: %s Version: %s HDependencies: %d Against: %s",
                this.id, this.version, dependencies.size(), buildAgainst);
        logger.debug(logMessage);
        return true;
    }

    public Class<?> getPluginClass() {
        return pluginClass;
    }

    public String getID() {
        return id;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<Method> getListeners() {
        return listeners;
    }

    public Map<Class<?>, List<Field>> getInjections() {
        return injections;
    }

    public PluginContainer newContainer() {
        return new PluginContainer(this);
    }
}
