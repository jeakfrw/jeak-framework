package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandExecutionContext extends CommandContext implements ICommandExecutionContext {

    private final Map<String, Object> parameters = new HashMap<>();
    private final IClient sender;

    public CommandExecutionContext(IClient sender) {
        this.sender = sender;
    }

    @Override
    public Optional<Object> getOne(String parameterName) {
        return Optional.ofNullable(parameters.getOrDefault(parameterName, null));
    }

    @Override
    public <T> Optional<T> getOne(String parameterName, Class<T> hint) {
        Object val = parameters.getOrDefault(parameterName, null);
        return Optional.ofNullable(val)
                .map(o -> hint.isAssignableFrom(o.getClass()) ? hint.cast(o) : null);
    }

    @Override
    public boolean hasOne(String fullName) {
        return getOne(fullName).isPresent();
    }

    @Override
    public Object putOrReplaceOne(String fullName, Object value) {
        return parameters.put(fullName, value);
    }

    public IClient getSender() {
        return sender;
    }
}
