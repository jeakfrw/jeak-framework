package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

import java.util.Optional;

public class StringParamMatcher implements IParameterMatcher<String> {

    @Override
    public Class<String> getSupportedType() {
        return String.class;
    }

    @Override
    public Optional<String> tryMatch(String paramString) {
        return Optional.ofNullable(paramString);
    }
}
