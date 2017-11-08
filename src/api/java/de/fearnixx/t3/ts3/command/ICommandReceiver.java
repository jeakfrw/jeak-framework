package de.fearnixx.t3.ts3.command;

import de.fearnixx.t3.event.query.IQueryEvent;

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
     * @param message The message which shall be interpreted as a command
     */
    void receive(IQueryEvent.INotification.ITargetClient.ITextMessage message);
}
