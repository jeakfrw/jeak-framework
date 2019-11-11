package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.event.query.QueryEvent;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandExecutionContext extends QueryEvent.ClientTextMessage implements ICommandExecutionContext {

    private final Map<String, Object> parameters = new HashMap<>();
    private final IClient sender;
    private final CommandInfo info;
    private final AtomicInteger parameterIndex = new AtomicInteger();

    public CommandExecutionContext(IClient sender, CommandInfo info, IUserService userSvc) {
        super(userSvc);
        this.sender = sender;
        this.info = info;
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

    public CommandInfo getCommandInfo() {
        return info;
    }

    public AtomicInteger getParameterIndex() {
        return parameterIndex;
    }
}
