package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.service.command.spec.ICommandInfo;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public interface ICommandExecutionContext extends IQueryEvent.INotification.IClientTextMessage {

    Optional<Object> getOne(String fullName);

    <T> Optional<T> getOne(String fullName, Class<T> hint);

    boolean hasOne(String fullName);

    Object putOrReplaceOne(String fullName, Object value);

    IClient getSender();

    ICommandInfo getCommandInfo();

    AtomicInteger getParameterIndex();
}
