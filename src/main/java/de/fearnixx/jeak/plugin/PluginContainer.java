package de.fearnixx.jeak.plugin;

import de.fearnixx.jeak.plugin.persistent.PluginRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.fearnixx.jeak.plugin.PluginContainer.State.INIT;


/**
 * Created by Life4YourGames on 23.05.17.
 */
public class PluginContainer {

    private final Class<?> myClass;

    private Object victim;

    private final Map<Class<?>, List<Field>> injections;

    public PluginContainer(PluginRegistry registry) {
        this.myClass = registry.getPluginClass();
        injections = registry.getInjections();
    }

    public void construct() throws Exception {
        victim = myClass.getDeclaredConstructor().newInstance();
    }

    public List<Field> getInjectionsFor(Class<?> forClass) {
        List<Field> res = new ArrayList<>();
        injections.forEach((c, l) -> {
            if (c.isAssignableFrom(forClass)) {
                res.addAll(l);
            }
        });
        return res;
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
