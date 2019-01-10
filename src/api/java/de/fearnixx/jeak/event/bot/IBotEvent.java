package de.fearnixx.jeak.event.bot;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.IEvent;

/**
 * Created by MarkL4YG on 29-Jan-18
 */
public interface IBotEvent extends IEvent {

    /**
     * Returns the bot from which the event originated
     */
    IBot getBot();
}
