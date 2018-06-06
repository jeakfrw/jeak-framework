package de.fearnixx.t3.event;

import de.fearnixx.t3.reflect.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author MarkL4YG
 */
public class EventListenerContainer {

    private Listener annotation;
    private Object victim;
    private Method method;
    private Class<IEvent> listensTo;

    public EventListenerContainer(Object victim, Method method) {

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalArgumentException("Cannot register listener " + victim.getClass() + '#'
                                               + method.getName() + ": Wrong number of parameters");
        }

        if (!IEvent.class.isAssignableFrom(paramTypes[0]))
            throw new IllegalArgumentException("Cannot register listener " + victim.getClass() + "#"
                                                + method.getName() + ": Wrong parameter type!");

        listensTo = (Class<IEvent>) paramTypes[0];
        this.method = method;
        this.annotation = method.getAnnotation(Listener.class);
    }

    public Short getOrder() {
        return annotation.order();
    }

    public Object getVictim() {
        return victim;
    }

    public Boolean accepts(Class<? extends IEvent> eventClass) {
        return listensTo.isAssignableFrom(eventClass);
    }

    public void accept(IEvent event) {
        try {
            method.invoke(victim, event);

        } catch (EventAbortException e) {
            throw e;

        } catch (Throwable e) {
            throw new EventInvocationException("Failed to pass \"" + event.getClass().getName() + "\" to " + method.toGenericString());
        }
    }
}
