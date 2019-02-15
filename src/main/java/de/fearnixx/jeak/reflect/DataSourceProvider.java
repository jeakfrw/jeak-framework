package de.fearnixx.jeak.reflect;


import de.fearnixx.jeak.service.database.DatabaseService;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
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

        String unitName = annotation.value();
        if (unitName.isEmpty()) {
            throw new IllegalArgumentException("Cannot inject EntityManager without unit ID!");
        }

        DatabaseService service = ctx.getServiceManager().provideUnchecked(DatabaseService.class);
        Optional<IPersistenceUnit> unit = service.getPersistenceUnit(unitName);
        Object value = null;

        if (clazz.isAssignableFrom(Boolean.class)) {
            value = unit.isPresent();

        } else if (!unit.isPresent()) {
            logger.warn("Persistence unit not available: {}", unitName);
            return Optional.empty();

        } else if (clazz.isAssignableFrom(IPersistenceUnit.class)) {
            value = unit.get();

        } else if (clazz.isAssignableFrom(javax.sql.DataSource.class)) {
            value = unit.get().getDataSource();

        } else if (clazz.isAssignableFrom(EntityManager.class)) {
            value = unit.get().getEntityManager();
        }

        return Optional.ofNullable(clazz.cast(value));
    }
}
