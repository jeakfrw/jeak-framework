package de.fearnixx.jeak.reflect;


import de.fearnixx.jeak.service.database.IDatabaseService;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import java.lang.reflect.Field;
import java.util.Optional;

public class DataSourceProvider extends AbstractSpecialProvider<PersistenceUnit> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);

    @Override
    public Class<PersistenceUnit> getAnnotationClass() {
        return PersistenceUnit.class;
    }

    @Override
    public Optional<Object> provideWith(InjectionContext ctx, Field field) {
        final PersistenceUnit annotation = field.getAnnotation(PersistenceUnit.class);
        final Class<?> clazz = field.getType();

        String unitName = annotation.name().isEmpty() ? annotation.unitName() : annotation.name();
        if (unitName.isEmpty()) {
            throw new IllegalArgumentException("Cannot inject EntityManager without unit ID!");
        }

        IDatabaseService service = ctx.getServiceManager().provideUnchecked(IDatabaseService.class);
        Optional<IPersistenceUnit> unit = service.getPersistenceUnit(unitName);
        Object value = null;

        if (clazz.isAssignableFrom(Boolean.class)) {
            value = unit.isPresent();

        } else if (unit.isEmpty()) {
            logger.warn("Persistence unit not available: {}", unitName);
            return Optional.empty();

        } else if (clazz.isAssignableFrom(IPersistenceUnit.class)) {
            value = unit.get();

        } else if (clazz.isAssignableFrom(javax.sql.DataSource.class)) {
            value = unit.get().getDataSource();

        } else if (clazz.isAssignableFrom(EntityManager.class)) {
            logger.warn("[DEPRECATION] Directly injecting entity managers is deprecated as it reserves connections. " +
                    "Entity managers should be retrieved and closed regularly. E.g. open with begin of a request/event and closed afterwards. " +
                    "This will ensure connections are returned to the pool.");
            value = unit.get().getEntityManager();
        }

        return Optional.ofNullable(clazz.cast(value));
    }
}
