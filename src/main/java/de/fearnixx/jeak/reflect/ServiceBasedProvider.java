package de.fearnixx.jeak.reflect;

import java.lang.reflect.Field;
import java.util.Optional;

public class ServiceBasedProvider {

    private final InjectionService injectionService;

    public ServiceBasedProvider(InjectionService injectionService) {
        this.injectionService = injectionService;
    }

    @SuppressWarnings("squid:S1452")
    public Optional<?> provideWith(InjectionContext ctx, Field field) {
        final Class<?> clazz = field.getType();
        Optional<?> result = ctx.getServiceManager().provide(clazz);
        if (result.isPresent()) {
            if (result.get() instanceof IInjectionService) {
                return Optional.of(clazz.cast(injectionService));
            } else {
                return result;
            }
        }

        return Optional.empty();
    }

    private Optional<String> checkForCtxId(Class<?> clazz) {
        final JeakBotPlugin plugin = clazz.getAnnotation(JeakBotPlugin.class);
        if (plugin != null) {
            return Optional.of(plugin.id());
        } else {
            return Optional.empty();
        }
    }
}
