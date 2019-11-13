package de.fearnixx.jeak.service.command.spec.matcher;

import java.util.Optional;

public interface IMatcherRegistryService {

    void registerMatcher(ICriterionMatcher<?> matcher);

    <T> Optional<ICriterionMatcher<T>> findForType(Class<T> matcher);
}
