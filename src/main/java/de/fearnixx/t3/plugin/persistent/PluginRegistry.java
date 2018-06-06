package de.fearnixx.t3.plugin.persistent;

import de.fearnixx.t3.event.IEvent;
import de.fearnixx.t3.plugin.PluginContainer;
import de.fearnixx.t3.reflect.Inject;
import de.fearnixx.t3.reflect.Listener;
import de.fearnixx.t3.reflect.T3BotPlugin;
import de.mlessmann.common.Common;
import de.mlessmann.logging.ILogReceiver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class PluginRegistry {

    private static ILogReceiver log;
    public static void setLog(ILogReceiver log) {
        if (PluginRegistry.log != null) {
            PluginRegistry.log.severe("Logger got hot-swapped!");
            log.severe("Hot-swapped in for old pr logger");
        }
        PluginRegistry.log = log;
    }

    public static Optional<PluginRegistry> getFor(Class<?> pluginClass) {
        PluginRegistry pr = new PluginRegistry(pluginClass);
        if (!pr.analyze()) return Optional.empty();
        return Optional.of(pr);
    }

    private Class<?> pluginClass;
    private T3BotPlugin tag;

    private String id;
    private String version;
    private String buildAgainst;
    private String breaksBefore;
    private String breaksAfter;
    private List<String> HARD_depends;
    private List<String> SOFT_depends;

    private List<Method> listeners;
    private Map<Class<?>, List<Field>> injections;

    private PluginRegistry(Class<?> pluginClass) {
        this.pluginClass = pluginClass;
    }

    protected boolean analyze() {
        log.fine("Analyzing class: ", pluginClass.toGenericString(), " from ", pluginClass.getProtectionDomain().getCodeSource().getLocation().getPath());
        log.finer("Reading tag");
        tag = pluginClass.getAnnotation(T3BotPlugin.class);
        if (tag == null) {
            log.severe("Attempt to analyze untagged plugin class: ", pluginClass.toGenericString());
            return false;
        }
        id = tag.id();
        if (!id.matches("^[a-z0-9.]+$")) {
            log.severe("Plugin ID: ", this.id, " is invalid!");
            return false;
        }
        version = Common.stripVersion(tag.version());
        if ("0".equals(version)) {
            log.warning("Plugin ID: ", this.id, " is using an invalid version: ", tag.version());
            version = null;
        }

        // TODO: Bot version verification implementation
        buildAgainst = tag.builtAgainst();
        breaksAfter = tag.breaksAfter();
        breaksBefore = tag.breaksBefore();

        if (tag.depends().length == 0) {
            HARD_depends = Collections.emptyList();
        } else {
            HARD_depends = Collections.unmodifiableList(Arrays.asList(tag.depends()));
        }
        if (tag.requireAfter().length == 0) {
            SOFT_depends = Collections.emptyList();
        } else {
            SOFT_depends = Collections.unmodifiableList(Arrays.asList(tag.requireAfter()));
        }


        log.finer("Pre-processing listeners");
        listeners = new ArrayList<>();

        Method[] methods = pluginClass.getDeclaredMethods();
        for (Method method : methods) {
            Annotation anno = method.getAnnotation(Listener.class);
            if (anno == null) continue;
            if (method.getParameterCount() != 1) {
                log.finest("Wrong parameter count for method: ", method.getName());
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                log.finest("Wrong visibility for method: ", method.getName());
                continue;
            }
            if (!IEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                log.finest("Wrong parameterType for method: ", method.getName());
                continue;
            }

            listeners.add(method);
        }

        log.finer("Pre-processing injections");
        injections = new HashMap<>();
        Field[] fields = pluginClass.getFields();
        for (Field field : fields) {
            if (field.getAnnotation(Inject.class) == null) continue;
            int mod = field.getModifiers();
            if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isFinal(mod) || Modifier.isVolatile(mod)) {
                log.finest("Wrong modifiers for field:", field.getName());
            }
            List<Field> l = injections.getOrDefault(field.getType(), null);
            if (l == null) {
                l = new ArrayList<>();
                injections.put(field.getType(), l);
            }
            l.add(field);
        }

        log.fine("Plugin class ", pluginClass.toGenericString(), " analysed.");
        log.finer("ID: ", this.id, " Version: " + this.version,
                " HDependencies: ", HARD_depends.size(),
                " SDependencies: ", SOFT_depends.size(),
                " Build-INFO:[", breaksBefore, ',', buildAgainst, ',', breaksAfter, ']');
        return true;
    }

    public Class<?> getPluginClass() {
        return pluginClass;
    }

    public String getID() {
        return id;
    }

    public List<String> getHARD_depends() {
        return HARD_depends;
    }

    public List<String> getSOFT_depends() {
        return SOFT_depends;
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
