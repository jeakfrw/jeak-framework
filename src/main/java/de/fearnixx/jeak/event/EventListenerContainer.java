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
    public static final boolean FAST_LAMBDAS_ENABLED = Main.getProperty("jeak.frw.enableLambdaEvents", false);

    private static final Map<String, BiConsumer<Object, IEvent>> lambdaCache = new ConcurrentHashMap<>();
    private static final String EVENT_PASS_FAILED_MSG = "Failed to pass \"%s\" to %s";
    private static final String EVENT_PASS_FAILED_ACCESS_MSG = "Failed to pass \"%s\" to %s. Access to listener was denied!";

    private final String listenerFQN;
    private final Listener annotation;
    private final Object victim;
    private final BiConsumer<Object, IEvent> eventConsumer;
    private final Class<IEvent> listensTo;

    public EventListenerContainer(Object victim, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalArgumentException(String.format("Cannot register listener %s#%s: Wrong number of parameters", victim.getClass(), method.getName()));
        }

        if (!IEvent.class.isAssignableFrom(paramTypes[0]))
            throw new IllegalArgumentException(String.format("Cannot register listener %s#%s: Wrong parameter type!", victim.getClass(), method.getName()));

        //noinspection unchecked - Assignable check is done above
        listensTo = (Class<IEvent>) paramTypes[0];
        this.annotation = method.getAnnotation(Listener.class);
        this.victim = victim;
        this.listenerFQN = method.getDeclaringClass().getName() + '#' + method.getName();

        eventConsumer = constructIfNotCached(listenerFQN, () -> constructConsumer(method));
    }

    /**
     * Constructs a consumer that can be invoked when an event is fired.
     * The consumer takes the listeners object instance as its first and the event as its second argument.
     */
    protected BiConsumer<Object, IEvent> constructConsumer(Method method) {
        if (FAST_LAMBDAS_ENABLED) {
            return buildLambdaConsumer(method);
        } else {
            return buildReflectiveConsumer(method);
        }
    }

    private BiConsumer<Object, IEvent> buildLambdaConsumer(Method method) {
        try {
            MethodHandles.Lookup lookupHandle = MethodHandles.lookup();
            MethodHandle listenerMethod = lookupHandle.unreflect(method);

            if (logger.isDebugEnabled()) {
                logger.debug("Trying to construct listener-lambda for: {}", listenerMethod.type().toMethodDescriptorString());
            }

            var callSite = LambdaMetafactory.metafactory(
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
    }

    /**
     * @deprecated Classical way of dynamically invoking methods. This one should be phased out in the future for the lambda factory.
     */
    @Deprecated
    private BiConsumer<Object, IEvent> buildReflectiveConsumer(Method method) {
        return (lambdaVictim, event) -> {
            try {
                method.invoke(lambdaVictim, event);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(String.format(EVENT_PASS_FAILED_ACCESS_MSG, event.getClass().getName(), listenerFQN), e);

            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    throw new RelayedInvokationException(e.getCause());
                }
                throw new RelayedInvokationException(String.format(EVENT_PASS_FAILED_MSG, event.getClass().getName(), listenerFQN), e);
            }
        };
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
            // These two have special handling - we want to catch them upstream.
            throw e;
        } catch (RelayedInvokationException e) {
            // We caught an invocation target exception (probably from the reflective method invocation)
            // Throw the actual cause.
            Throwable cause = e.getCause();
            throw new EventInvocationException(String.format(EVENT_PASS_FAILED_MSG, event.getClass().getName(), listenerFQN), cause);
        } catch (Exception e) {
            // We caught ANY other exception.
            // Rethrow this as something unchecked which the service is aware of so we don't block other listeners.
            throw new EventInvocationException(String.format(EVENT_PASS_FAILED_MSG, event.getClass().getName(), listenerFQN), e);
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
