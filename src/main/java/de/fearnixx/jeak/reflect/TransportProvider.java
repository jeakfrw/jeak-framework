package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.mail.IMailService;
import de.fearnixx.jeak.service.mail.ITransportUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;

public class TransportProvider extends AbstractSpecialProvider<TransportUnit> {

    private static final Logger logger = LoggerFactory.getLogger(TransportProvider.class);

    @Override
    public Class<TransportUnit> getAnnotationClass() {
        return TransportUnit.class;
    }

    @Override
    public Optional<Object> provideWith(InjectionContext ctx, Field field) {
        TransportUnit annot = field.getAnnotation(TransportUnit.class);
        String unitName = annot.name();
        Optional<ITransportUnit> optUnit = ctx.getServiceManager()
                .provideUnchecked(IMailService.class).getTransportUnit(unitName);

        if (optUnit.isPresent()) {
            return Optional.of(optUnit.get());
        } else {
            logger.info("Transport unit \"{}\" not found for context \"{}\"", unitName, ctx.getContextId());
            return Optional.empty();
        }
    }
}
