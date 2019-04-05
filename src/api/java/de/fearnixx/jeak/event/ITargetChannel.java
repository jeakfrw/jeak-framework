package de.fearnixx.jeak.event;

import de.fearnixx.jeak.teamspeak.data.IChannel;

/**
 * Event that targeted a specific channel.
 * The target is injected using a SystemListener.
 * In case the injection fails this event will not be fired to preserve consistency.
 */
public interface ITargetChannel extends IEvent {

    IChannel getTarget();
}
