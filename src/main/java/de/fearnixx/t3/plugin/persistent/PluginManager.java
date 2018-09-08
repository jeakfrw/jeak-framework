package de.fearnixx.t3.plugin.persistent;

import de.fearnixx.t3.reflect.T3BotPlugin;
import de.mlessmann.logging.ILogReceiver;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class PluginManager {

    // * * * STATICS * * * //

    private static volatile PluginManager INST;

    public static PluginManager getInstance() {
        if (INST == null) {
            initialize(ILogReceiver.Dummy.newDummy());
        }
        return INST;
    }

    public static void initialize(ILogReceiver log) {
        INST = new PluginManager(log);
    }


    // * * * FIELDS * * * //

    private ILogReceiver log;
    private List<File> sources;
    private List<URL> urlList;
    private boolean includeCP;
    private ClassLoader pluginClassLoader;
    private Map<String, PluginRegistry> registryMap;

    // * * * CONSTRUCTION * * * //

    public PluginManager(ILogReceiver log) {
        this.log = log;
        registryMap = new HashMap<>();
        sources = new ArrayList<>();
        urlList = new ArrayList<>();
    }

    public void addSource(File dir) {
        if (dir.exists())
            sources.add(dir);
    }

    public List<File> getSources() {
        return sources;
    }

    public void load() {
        if (registryMap.size() > 0) {
            return;
        }
        PluginRegistry.setLog(log.getChild("REG"));
        scanPluginSources();

        List<Class<?>> candidates = new ArrayList<>();
        Reflections reflect = getPluginScanner(getPluginClassLoader());

        candidates.addAll(reflect.getTypesAnnotatedWith(T3BotPlugin.class, true));
        log.info(candidates.size(), " candidates found");
        candidates.forEach(c -> {
            Optional<PluginRegistry> r = PluginRegistry.getFor(c);
            if (r.isPresent()) {
                if (registryMap.containsKey(r.get().getID())) {
                    log.warning("Duplicate plugin ID found! ", r.get().getID());
                    return;
                }
                registryMap.put(r.get().getID(), r.get());
            }
        });
    }

    public ClassLoader getPluginClassLoader() {
        if (pluginClassLoader == null) {
            if (includeCP) {
                pluginClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), PluginManager.class.getClassLoader());
            } else {
                pluginClassLoader = new URLClassLoader(urlList.toArray(new URL[0]));
            }
        }
        return pluginClassLoader;
    }

    public Reflections getPluginScanner(ClassLoader classLoader) {
        ConfigurationBuilder builder = new ConfigurationBuilder()
                .addUrls(urlList)
                .addClassLoader(classLoader)
                .setScanners(new TypeElementsScanner(), new SubTypesScanner(false), new TypeAnnotationsScanner());

        if (includeCP) {
            log.info("Including classpath");
            builder.addUrls(ClasspathHelper.forClassLoader());
        }
        return new Reflections(builder);
    }

    public List<URL> getPluginUrls() {
        return urlList;
    }

    private void scanPluginSources() {
        if (sources.isEmpty()) {
            log.warning("No sources defined!");
        } else {
            sources.forEach(f -> {
                try {
                    if (f.isFile() && f.getName().endsWith(".jar"))
                        urlList.add(f.toURI().toURL());
                    else if (f.isDirectory()) {
                        File[] files = f.listFiles(f2 -> f2.getName().endsWith(".jar"));
                        if (files != null) {
                            for (File f2 : files) {
                                urlList.add(f2.toURI().toURL());
                            }
                        }
                    } else {
                        log.warning("Skipping plugin source,", f.getAbsolutePath());
                    }
                } catch (MalformedURLException e) {
                    log.warning(e);
                }
            });
        }
    }

    public Map<String, PluginRegistry> getAllPlugins() {
        return Collections.unmodifiableMap(registryMap);
    }

    public Optional<PluginRegistry> getPluginById(String id) {
        return Optional.ofNullable(registryMap.getOrDefault(id, null));
    }

    public int estimateCount() {
        return registryMap.size();
    }

    public boolean isIncludeCP() {
        return includeCP;
    }

    public void setIncludeCP(boolean includeCP) {
        this.includeCP = includeCP;
    }
}
