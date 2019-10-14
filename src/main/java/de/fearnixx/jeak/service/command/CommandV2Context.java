package de.fearnixx.jeak.service.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandV2Context extends CommandContext {

    private final Map<String, Object> parameters = new HashMap<>();

    public Optional<Object> getOne(String parameterName) {
        return Optional.ofNullable(parameters.getOrDefault(parameterName, null));
    }

    public <T> Optional<T> getOne(String parameterName, Class<T> hint) {
        Object val = parameters.getOrDefault(parameterName, null);
        return Optional.ofNullable(val)
                .map(o -> hint.isAssignableFrom(o.getClass()) ? hint.cast(o) : null);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
