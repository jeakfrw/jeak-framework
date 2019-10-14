package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

import java.util.Optional;

public class BooleanParamMatcher implements IParameterMatcher<Boolean> {

    @Override
    public Class<Boolean> getSupportedType() {
        return Boolean.class;
    }

    @Override
    public Optional<Boolean> tryMatch(String paramString) {
        if ("t".equals(paramString) || "1".equals(paramString) || "true".equals(paramString)
                || "y".equals(paramString) || "yes".equals(paramString)) {
            return Optional.of(Boolean.TRUE);
        } else if ("f".equals(paramString) || "0".equals(paramString) || "false".equals(paramString)
                || "n".equals(paramString) || "no".equals(paramString)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }
}
