package de.fearnixx.jeak.plugin.persistent;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import io.github.classgraph.ClassGraph;
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
    private static final boolean VERBOSE_SCAN = Main.getProperty("jeak.pluginmanager.verboseScan", false);
    private static final String MARKER_ANNOTATION_NAME = JeakBotPlugin.class.getName();
    private static final String[] EXCLUDED_PACKAGES = {
            "sun",
            "java",
            "com.google",
            "com.fasterxml",
            "com.oracle",
            "com.sun",
            "com.ibm",
            "net.bytebuddy",
            "net.jcip",
            "org.jboss",
            "org.classpath",
            "org.dom4j",
            "org.ietf",
            "org.reflections",
            "org.slf4j",
            "org.w3c",
            "org.xml",
            "org.omg",
            "org.eclipse"
    };

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

    private final List<File> sources = new ArrayList<>();
    private final List<URL> urlList = new ArrayList<>();
    private ClassLoader pluginClassLoader;
    private final Map<String, PluginRegistry> registryMap = new HashMap<>();

    // * * * CONSTRUCTION * * * //

    public void addSource(File dir) {
        if (dir.exists())
            sources.add(dir);
    }

    public void load() {
        if (registryMap.size() > 0) {
            return;
        }
        scanPluginSources();

        logger.debug("Scanning for plugin classes.");
        final var scanner = getPluginScanner();
        try (final var result = scanner.scan()) {
            final var candidates = result.getClassesWithAnnotation(MARKER_ANNOTATION_NAME).loadClasses(true);
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
    }

    public ClassGraph getPluginScanner() {
        return new ClassGraph()
                .verbose(VERBOSE_SCAN)
                .enableClassInfo()
                .enableAnnotationInfo()
                .overrideClassLoaders(pluginClassLoader, ClassLoader.getSystemClassLoader())
                .rejectPackages(EXCLUDED_PACKAGES);
    }

    private void scanPluginSources() {
        if (sources.isEmpty()) {
            logger.warn("No sources defined!");
        } else {
            sources.forEach(f -> {
                try {
                    if (f.isFile() && f.getName().endsWith(".jar")) {
                        final var jarURL = f.toURI().toURL();
                        logger.debug("Found plugin jar: {}", jarURL);
                        urlList.add(jarURL);
                    } else if (f.isDirectory()) {
                        File[] files = f.listFiles(f2 -> f2.getName().endsWith(".jar"));
                        if (files != null) {
                            for (File f2 : files) {
                                final var jarURL = f2.toURI().toURL();
                                logger.debug("Found plugin jar: {}", jarURL);
                                urlList.add(jarURL);
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
        pluginClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), getClass().getClassLoader());
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

    public ClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }
}
