package de.fearnixx.jeak.reflect;

import java.lang.reflect.Field;
import java.util.Optional;

public class ServiceBasedProvider {

    private InjectionService manager;

    public ServiceBasedProvider(InjectionService manager) {
        this.manager = manager;
    }

    @SuppressWarnings("squid:S1452")
    public Optional<?> provideWith(InjectionContext ctx, Field field) {
        final Class<?> clazz = field.getType();
        Optional<?> result = ctx.getServiceManager().provide(clazz);
        if (result.isPresent()) {
            if (result.get() instanceof IInjectionService) {
                InjectionService value = provideInjectionSvc(ctx, field);
                return Optional.of(clazz.cast(value));
            } else {
                return result;
            }
        }

        return Optional.empty();
    }

    protected InjectionService provideInjectionSvc(InjectionContext ctx, Field field) {
        final String ctxId = checkForCtxId(field.getDeclaringClass())
                .map(id -> {
                    if (id.contains(".")) {
                        return id.substring(id.lastIndexOf('.') + 1);
                    } else {
                        return id;
                    }
                })
                .orElse(ctx.getContextId());

        return manager.getChild(ctxId);
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
