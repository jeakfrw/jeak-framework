package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 02-Feb-18
 */
public class InjectionService implements IInjectionService {

    public static final Boolean UNIT_FULLY_QUALIFIED = Main.getProperty("bot.inject.fqunit", Boolean.FALSE);

    private static final Logger logger = LoggerFactory.getLogger(InjectionService.class);

    private final FieldSearch searchAlgo = new FieldSearch();
    private final InjectionContext injectionContext;
    private final List<AbstractSpecialProvider<?>> providers = new LinkedList<>();
    private final ServiceBasedProvider fallbackProvider = new ServiceBasedProvider(this);

    public InjectionService(InjectionContext injectionContext) {
        this.injectionContext = injectionContext;
    }

    public void addProvider(AbstractSpecialProvider<?> provider) {
        if (!providers.contains(provider)) {
            providers.add(provider);
        }
    }

    @Override
    public <T> T injectInto(T victim) {
        if (logger.isDebugEnabled()) {
            logger.debug("Running injections on object of class: {}", victim.getClass().getName());
        }

        Class<?> clazz = victim.getClass();
        JeakBotPlugin plugin = clazz.getAnnotation(JeakBotPlugin.class);
        if (plugin != null && proxySubContextInjection(victim, plugin) != null) {
            return victim;
        }

        final List<Field> fields = searchAlgo.getAnnotatedFields(clazz);
        for (Field field : fields) {
            // Check if there's a special injection to run
            final Optional<AbstractSpecialProvider<?>> optSpecialProvider =
                    providers.stream()
                            .filter(provider -> provider.test(field))
                            .findFirst();

            // Evaluate value
            Optional<?> optTarget;
            if (optSpecialProvider.isPresent()) {
                optTarget = optSpecialProvider.get().provideWith(injectionContext, field);
            } else {
                optTarget = fallbackProvider.provideWith(injectionContext, field);
            }

            // Inject the value
            optTarget.ifPresent(value -> {
                try {
                    setFieldValue(victim, field, value);
                } catch (IllegalAccessException e) {
                    logger.error("Failed injection of class {} into object of class {}", field.getType(), clazz.toString(), e);
                }
            });
        }
        return victim;
    }

    private <T> void setFieldValue(T victim, Field field, Object value) throws IllegalAccessException {
        // Access state
        boolean accessState = field.isAccessible();
        field.setAccessible(true);

        field.set(victim, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Injected {} as {}", value.getClass().getCanonicalName(), field.getName());
        }

        // Reset access state
        field.setAccessible(accessState);
    }

    private <T> T proxySubContextInjection(T victim, JeakBotPlugin plugin) {
        String id = plugin.id();
        if (id.contains(".")) {
            id = id.substring(id.lastIndexOf('.') + 1);
        }

        if (!id.equals(injectionContext.getContextId())) {
            logger.debug("Proxying injection context: {}", id);
            final InjectionContext childCtx = injectionContext.getChild(id);
            final InjectionService child = new InjectionService(childCtx);
            providers.forEach(child::addProvider);
            return child.injectInto(victim);
        }

        return null;
    }
}
