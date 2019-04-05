package de.fearnixx.jeak.event;

import de.fearnixx.jeak.teamspeak.data.IClient;

/**
 * Event that targeted a specific client.
 * The target is injected using a SystemListener.
 * In case the injection fails this event will not be fired to preserve consistency.
 */
public interface ITargetClient extends IEvent {

    IClient getTarget();
}
