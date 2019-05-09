package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.locale.ILocalizationService;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;

public class LocalizationProvider extends AbstractSpecialProvider<LocaleUnit> {

    private static final Logger logger = LoggerFactory.getLogger(LocalizationProvider.class);

    @Override
    public Class<LocaleUnit> getAnnotationClass() {
        return LocaleUnit.class;
    }

    @Override
    public Optional<Object> provideWith(InjectionContext ctx, Field f) {
        LocaleUnit unitSpec = f.getAnnotation(LocaleUnit.class);
        String resourceURI = unitSpec.defaultResource().trim();
        String unitId = unitSpec.value();

        if (unitId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot inject localization unit without id!");
        }

        ILocalizationUnit unit = ctx.getServiceManager()
                .provideUnchecked(ILocalizationService.class)
                .registerUnit(unitId);

        if (!resourceURI.isEmpty()) {
            logger.debug("Eagerly loading defaults for injected localization unit: {}", unitId);
            unit.loadDefaultsFromResource(ctx.getClassLoader(), resourceURI);
        }

        return Optional.of(unit);
    }
}
