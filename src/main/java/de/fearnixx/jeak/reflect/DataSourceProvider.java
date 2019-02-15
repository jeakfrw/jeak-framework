package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.Optional;

public class DataSourceProvider extends AbstractSpecialProvider<DataSource> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);

    @Override
    public Class<DataSource> getAnnotationClass() {
        return DataSource.class;
    }

    @Override
    public Optional<Object> provideWith(InjectionContext ctx, Field field) {
        final DataSource annotation = field.getAnnotation(DataSource.class);
        final Class<?> clazz = field.getType();

        if (annotation.value().isEmpty()) {
            throw new IllegalArgumentException("Cannot inject EntityManager without unit ID!");
        }

        DatabaseService service = ctx.getServiceManager().provideUnchecked(DatabaseService.class);
        Optional<EntityManager> manager = service.getEntityManager(annotation.value());
        Object value = null;

        if (clazz.isAssignableFrom(EntityManager.class)) {
            if (!manager.isPresent()) {
                final IllegalStateException except = new IllegalStateException("Failed to find persistence unit: " + annotation.value());
                logger.warn("PersistenceInjection failed", except);
                return Optional.empty();
            }
            value = manager.get();

        } else if (clazz.isAssignableFrom(Boolean.class)) {
            value = manager.isPresent();

        }

        return Optional.ofNullable(clazz.cast(value));
    }
}
