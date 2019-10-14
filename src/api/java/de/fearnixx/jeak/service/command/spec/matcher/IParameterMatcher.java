package de.fearnixx.jeak.service.command.spec.matcher;

import java.util.Optional;

public interface IParameterMatcher<T> {

    Class<T> getSupportedType();

    Optional<T> tryMatch(String paramString);
}
