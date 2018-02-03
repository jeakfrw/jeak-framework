package de.fearnixx.t3.service.command;

import de.fearnixx.t3.event.IQueryEvent;

public interface ICommandReceiver {

    /**
     * Receive a clients TextMessage. Can come from the following chat types:
     * * Private text
     * * Channel text
     * * Server text
     *
     * Commands must start with "!" in order to be received.
     * CommandReceivers only receive the commands they are registered for.
     *
     * @param event The message which shall be interpreted as a command
     */
    void receive(IQueryEvent.INotification.ITextMessage event);
}
