package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherRegistryService;
import de.fearnixx.jeak.service.command.spec.matcher.ICriterionMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkService(serviceInterface = IMatcherRegistryService.class)
public class MatcherRegistry implements IMatcherRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(MatcherRegistry.class);

    private final Map<Class<?>, ICriterionMatcher<?>> matchers = new ConcurrentHashMap<>();

    public synchronized void registerMatcher(ICriterionMatcher<?> matcher) {
        Class<?> supportedType = matcher.getSupportedType();
        var prev = matchers.put(supportedType, matcher);
        if (prev != null) {
            logger.info("Matcher for type \"{}\" ({}) has been replaced with: {}",
                    supportedType.getSimpleName(), prev.getClass().getName(), matcher.getClass().getName());
        }
    }

    @Override
    public synchronized <T> Optional<ICriterionMatcher<T>> findForType(Class<T> type) {
        return matchers.entrySet()
                .stream()
                .filter(e -> type.isAssignableFrom(e.getKey()))
                .map(Map.Entry::getValue)
                .map(e -> (ICriterionMatcher<T>) e)
                .findFirst();
    }

    public synchronized <T> ICriterionMatcher<T> getForType(Class<T> type) {
        return findForType(type)
                .orElseThrow(() -> new IllegalStateException("No matchers registered for type: " + type.getName()));
    }
}
