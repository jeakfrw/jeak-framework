package de.fearnixx.t3.plugin.persistent;

import de.fearnixx.t3.reflect.T3BotPlugin;
import de.mlessmann.logging.ILogReceiver;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
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
    private Map<String, PluginRegistry> registryMap;

    // * * * CONSTRUCTION * * * //

    public PluginManager(ILogReceiver log) {
        this.log = log;
        registryMap = new HashMap<>();
        sources = new ArrayList<>();
    }

    public void addSource(File dir) {
        if (dir.exists())
            sources.add(dir);
    }

    public void load(boolean includeCP) {
        if (registryMap.size() > 0) {
            return;
        }
        PluginRegistry.setLog(log.getChild("REG"));

        List<URL> urlList = new ArrayList<>();
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
        URL[] urls = urlList.toArray(new URL[urlList.size()]);
        List<Class<?>> candidates = new ArrayList<>();
        if (urls.length == 0) {
            log.warning("No sources defined!");
        } else {
            URLClassLoader loader = new URLClassLoader(urls);
            ConfigurationBuilder builder = new ConfigurationBuilder()
                    .addUrls(urls)
                    .addClassLoader(loader)
                    .setScanners(new TypeElementsScanner(), new SubTypesScanner(false), new TypeAnnotationsScanner());
            Reflections reflect = new Reflections(builder);

        /* WORK-AROUND for #getTypesAnnotatedWith(T3BotPlugin.class) returning an empty set */
            candidates.addAll(reflect.getTypesAnnotatedWith(T3BotPlugin.class, true));
            /*Set<String> classNames = reflect.getAllTypes();
            classNames.parallelStream().forEach(n -> {
                try {
                    Class<?> c = loader.loadClass(n);
                    if (c.getAnnotation(T3BotPlugin.class) != null) {
                        log.finest("Adding plugin class: ", n);
                        candidates.add(c);
                    } else {
                        log.finer("Ignoring class: ", n, " Plugin annotation missing");
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("ClassNotFound on result of #getAllTypes! Something has been completely broken!");
                }
            });*/
        }
        if (includeCP) {
            log.info("Including classpath");
            Reflections r = new Reflections();
            candidates.addAll(r.getTypesAnnotatedWith(T3BotPlugin.class));
        }

        log.info(candidates.size(), "candidates found");
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

    public Map<String, PluginRegistry> getAllPlugins() {
        Map<String, PluginRegistry> nMap = new HashMap<>();
        nMap.putAll(registryMap);
        return nMap;
    }

    public Optional<PluginRegistry> getPluginById(String id) {
        return Optional.ofNullable(registryMap.getOrDefault(id, null));
    }

    public int estimateCount() {
        return registryMap.size();
    }
}
