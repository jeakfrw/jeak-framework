package de.fearnixx.t3.reflect.plugins;

import de.fearnixx.t3.reflect.listeners.ListenerContainer;
import de.fearnixx.t3.reflect.plugins.persistent.PluginRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.fearnixx.t3.reflect.plugins.PluginContainer.State.INIT;

/**
 * Created by Life4YourGames on 23.05.17.
 */
public class PluginContainer {

    private PluginRegistry myReg;
    private Class<?> myClass;

    private Object victim;
    private ListenerContainer pluginListener;

    private Map<Class<?>, List<Field>> injections;

    public PluginContainer(PluginRegistry registry) {
        this.myReg = registry;
        this.myClass = registry.getPluginClass();

        injections = registry.getInjections();
    }

    public void construct() throws Exception {
        victim = myClass.getDeclaredConstructor().newInstance();
        pluginListener = new ListenerContainer(victim, myReg.getListeners());
    }

    public List<Field> getInjectionsFor(Class<?> forClass) {
        List<Field> res = new ArrayList<>();
        injections.forEach((c, l) -> {
            if (c.isAssignableFrom(forClass))
                res.addAll(l);
        });
        return res;
    }

    public ListenerContainer getListener() {
        return pluginListener;
    }

    private State state = INIT;
    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public Object getPlugin() {
        return victim;
    }

    public enum State {
        INIT,
        DEPENDENCIES,
        REQUIRE_AFTER,
        INJECT,
        DONE,
        FAILED_DEP,
        FAILED
    }
}
