package de.fearnixx.t3.event;

import de.fearnixx.t3.event.IEvent;
import de.mlessmann.logging.ILogReceiver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 01.06.17.
 */
public class EventListener {

    private Class<? extends Annotation> annotation;
    private List<Method> listeners;
    private final Map<Class<? extends IEvent>, List<Method>> mappedListeners = new HashMap<>();
    private Object victim;

    public EventListener(Object o, List<Method> listeners) {
        if (o == null) throw new IllegalArgumentException("Object is null");
        this.victim = o;
        this.listeners = listeners;
    }

    public EventListener(ILogReceiver log, Class<? extends Annotation> annotation, Object o) {
        log.finer("Analyzing object of class: ", o.getClass().toGenericString());
        this.victim = o;
        listeners = new ArrayList<>();

        Method[] methods = o.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getAnnotation(annotation) == null) continue;
            if (method.getParameterCount() != 1) {
                log.finest("Wrong parameter count for method: ", method.getName());
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                log.finest("Wrong visibility for method: ", method.getName());
                continue;
            }
            if (!IEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                log.finest("Wrong parameterType for method: " + method.getName());
                continue;
            }
            listeners.add(method);
        }
    }

    public boolean hasAny() {
        return !mappedListeners.isEmpty();
    }

    public Object getVictim() {
        return victim;
    }

    public void fireEvent(IEvent event) throws Exception {
        Class<? extends IEvent> cls = event.getClass();
        boolean refire = false;
        synchronized (mappedListeners) {
            if (mappedListeners.containsKey(cls)) {
                List<Method> l = mappedListeners.get(cls);
                for (Method method : l) {
                    method.invoke(victim, event);
                }
            } else {
                List<Method> list = new ArrayList<>();
                for (Method m : listeners) {
                    if (m.getParameterTypes()[0].isAssignableFrom(cls)) {
                        list.add(m);
                    }
                }
                mappedListeners.put(cls, list);
                refire = true;
            }
        }
        if (refire) fireEvent(event);
    }
}
