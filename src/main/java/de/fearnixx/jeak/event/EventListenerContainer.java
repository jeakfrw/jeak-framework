package de.fearnixx.jeak.event;

import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
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

        //noinspection unchecked - Assignable check is done above
        listensTo = (Class<IEvent>) paramTypes[0];
        this.method = method;
        this.annotation = method.getAnnotation(Listener.class);
        this.victim = victim;
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

            try {
                method.invoke(victim, event);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw ((Exception) e.getCause());
                } else {
                    throw new RuntimeException(e);
                }
            }

        } catch (EventAbortException|ConsistencyViolationException e) {
            throw e;

        } catch (Exception e) {
            throw new EventInvocationException("Failed to pass \"" + event.getClass().getName() + "\" to " + method.toGenericString(), e);
        }
    }
}
