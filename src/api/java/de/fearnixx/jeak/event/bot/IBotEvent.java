package de.fearnixx.jeak.event.bot;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.IEvent;

/**
 * An event that is associated with the frameworks bot instance.
 */
public interface IBotEvent extends IEvent {

    /**
     * Returns the bot from which the event originated.
     */
    IBot getBot();
}
