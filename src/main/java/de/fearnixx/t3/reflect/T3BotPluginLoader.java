package de.fearnixx.t3.reflect;

import de.fearnixx.t3.reflect.annotation.T3BotPlugin;
import de.mlessmann.logging.ILogReceiver;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by Life4YourGames on 22.05.17.
 *
 * This class is supposed to load the plugins from the disk
 * Initialization or dependency-check is not done by this class
 */
public class T3BotPluginLoader {

    private boolean includeCP;
    private List<File> sources;
    private ILogReceiver log;

    public T3BotPluginLoader(ILogReceiver log) {
        sources = new ArrayList<>();
        this.log = log;
    }

    public void reset() {
        sources.clear();
    }

    public void addDir(String directory) {
        File f = new File(directory);
        if (f.isDirectory() && f.canRead()) {
            sources.add(f);
        }
    }

    public void addPlugin(String jar) {
        File f = new File(jar);
        if (f.isFile() && f.canRead() && f.canExecute()) {
            sources.add(f);
        }
    }

    public Set<Class<?>> getPlugins() {
        List<URL> urls = new ArrayList<>(60);
        sources.forEach(s -> {
            try {
                if (s.isFile() && s.getName().endsWith(".jar") && s.canRead() && s.canExecute()) {
                    urls.add(s.toURI().toURL());
                }
                if (s.isDirectory() && s.canRead()) {
                    File[] a = s.listFiles(f -> f.getName().endsWith(".jar") && f.canRead() && f.canExecute());
                    if (a != null) {
                        for (File f : a) {
                            urls.add(f.toURI().toURL());
                        }
                    }
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        URLClassLoader loader;
        if (includeCP) {
            loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
        } else {
            loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
        }
        ConfigurationBuilder refConf = new ConfigurationBuilder();
        refConf.setClassLoaders(new ClassLoader[]{loader});
        Reflections ref = new Reflections();
        Set<Class<?>> classes = ref.getTypesAnnotatedWith(T3BotPlugin.class);
        return Collections.unmodifiableSet(classes);
    }
}
