package de.fearnixx.jeak.service.command.spec.matcher;

import java.util.Optional;

public interface IMatcherRegistryService {

    void registerMatcher(IParameterMatcher<?> matcher);

    <T> Optional<IParameterMatcher<T>> findForType(Class<T> matcher);
}
