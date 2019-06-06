package de.fearnixx.jeak.event;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.except.EventAbortException;
import de.fearnixx.jeak.event.except.EventInvocationException;
import de.fearnixx.jeak.event.except.ListenerConstructionException;
import de.fearnixx.jeak.event.except.RelayedInvokationException;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 *
 */
public class EventListenerContainer {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerContainer.class);
    private static final MethodType LISTENER_INTERFACE_TYPE = MethodType.methodType(BiConsumer.class);
    private static final MethodType LISTENER_LAMBDA_METHOD_TYPE = MethodType.methodType(void.class, Object.class, Object.class);
    public static final Boolean FAST_LAMBDAS_ENABLED = Main.getProperty("jeak.frw.enableLambdaEvents", false);

    private static final Map<String, BiConsumer<Object, IEvent>> lambdaCache = new ConcurrentHashMap<>();

    private String listenerFQN;
    private Listener annotation;
    private Object victim;
    private BiConsumer<Object, IEvent> eventConsumer;
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
        this.annotation = method.getAnnotation(Listener.class);
        this.victim = victim;
        this.listenerFQN = method.getDeclaringClass().getName() + '#' + method.getName();

        eventConsumer = constructIfNotCached(listenerFQN, () -> constructLambda(method));
    }

    private BiConsumer<Object, IEvent> constructLambda(Method method) {
        if (FAST_LAMBDAS_ENABLED) {
            try {
                MethodHandles.Lookup lookupHandle = MethodHandles.lookup();
                MethodHandle listenerMethod = lookupHandle.unreflect(method);

                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to construct listener-lambda for: {}", listenerMethod.type().toMethodDescriptorString());
                }

                CallSite callSite = LambdaMetafactory.metafactory(
                        lookupHandle,
                        "accept",
                        LISTENER_INTERFACE_TYPE,
                        LISTENER_LAMBDA_METHOD_TYPE,
                        listenerMethod,
                        listenerMethod.type());
                MethodHandle factory = callSite.getTarget();
                // Lambda will match the provided signature.
                //noinspection unchecked
                return (BiConsumer<Object, IEvent>) factory.invoke();

            } catch (IllegalAccessError e) {
                String msg = MessageFormat.format("Failed to get method handle for listener: {0}", listenerFQN);
                throw new ListenerConstructionException(msg, e);
            } catch (LambdaConversionException e) {
                String msg = MessageFormat.format("Failed to construct lambda for event listener: {0}", listenerFQN);
                throw new ListenerConstructionException(msg, e);
            } catch (Throwable e) {
                String msg = MessageFormat.format("Failed to get lambda for event listener: {0}", listenerFQN);
                throw new ListenerConstructionException(msg, e);
            }
        } else {
            return (lambdaVictim, event) -> {
                try {
                    method.invoke(lambdaVictim, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RelayedInvokationException(e);
                }
            };
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug("Sending event \"{}\" to listener: {}", event.getClass().getName(), listenerFQN);
        }
        try {
            eventConsumer.accept(victim, event);
        } catch (EventAbortException | ConsistencyViolationException e) {
            throw e;
        } catch (RelayedInvokationException e) {
            throw new EventInvocationException("Failed to pass \"" + event.getClass().getName() + "\" to " + listenerFQN, e.getCause());
        } catch (Exception e) {
            throw new EventInvocationException("Failed to pass \"" + event.getClass().getName() + "\" to " + listenerFQN, e);
        }
    }

    public String getListenerFQN() {
        return listenerFQN;
    }

    private static BiConsumer<Object, IEvent> constructIfNotCached(String methodFQN, Supplier<BiConsumer<Object, IEvent>> supplier) {
        synchronized (lambdaCache) {
            return lambdaCache.computeIfAbsent(methodFQN, fqn -> supplier.get());
        }
    }
}
