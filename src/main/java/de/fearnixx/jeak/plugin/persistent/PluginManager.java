package de.fearnixx.jeak.plugin.persistent;

import de.fearnixx.jeak.reflect.JeakBotPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private static volatile PluginManager INST;

    public static PluginManager getInstance() {
        if (INST == null) {
            initialize();
        }
        return INST;
    }

    public static void initialize() {
        INST = new PluginManager();
    }


    // * * * FIELDS * * * //

    private List<File> sources = new ArrayList<>();
    private List<URL> urlList = new ArrayList<>();
    private boolean includeCP;
    private ClassLoader pluginClassLoader;
    private Map<String, PluginRegistry> registryMap = new HashMap<>();

    // * * * CONSTRUCTION * * * //

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
        scanPluginSources();

        List<Class<?>> candidates = new ArrayList<>();
        Reflections reflect = getPluginScanner(getPluginClassLoader());

        candidates.addAll(reflect.getTypesAnnotatedWith(JeakBotPlugin.class, true));
        logger.info("Found {} plugin candidates", candidates.size());
        candidates.forEach(c -> {
            Optional<PluginRegistry> r = PluginRegistry.getFor(c);
            if (r.isPresent()) {
                if (registryMap.containsKey(r.get().getID())) {
                    logger.warn("Duplicate plugin ID found! {}", r.get().getID());
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
            logger.info("Including classpath");
            builder.addUrls(ClasspathHelper.forClassLoader(ClassLoader.getSystemClassLoader()));
        }
        return new Reflections(builder);
    }

    public List<URL> getPluginUrls() {
        return urlList;
    }

    private void scanPluginSources() {
        if (sources.isEmpty()) {
            logger.warn("No sources defined!");
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
                        logger.warn("Skipping plugin source: {}", f.getAbsolutePath());
                    }
                } catch (MalformedURLException e) {
                    logger.warn("Failed to construct plugin URL. HOW DID YOU DO THIS???", e);
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
