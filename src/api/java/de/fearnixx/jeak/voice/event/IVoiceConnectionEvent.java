package de.fearnixx.jeak.voice.event;

import de.fearnixx.jeak.event.IEvent;

/**
 * Represents an event that is related to a voice connection
 */
public interface IVoiceConnectionEvent extends IEvent {

    /**
     * @return the identifier of the voice connection the message got sent to
     */
    String getVoiceConnectionIdentifier();
}
