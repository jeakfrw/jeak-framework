package de.fearnixx.jeak.service.command;

import java.util.Optional;

public interface ICommandExecutionContext extends ICommandContext {

    Optional<Object> getOne(String fullName);

    <T> Optional<T> getOne(String fullName, Class<T> hint);

    boolean hasOne(String fullName);
}
