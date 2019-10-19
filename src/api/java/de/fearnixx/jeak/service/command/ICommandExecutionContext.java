package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Optional;

public interface ICommandExecutionContext extends ICommandContext {

    Optional<Object> getOne(String fullName);

    <T> Optional<T> getOne(String fullName, Class<T> hint);

    boolean hasOne(String fullName);

    Object putOrReplaceOne(String fullName, Object value);

    IClient getSender();
}
