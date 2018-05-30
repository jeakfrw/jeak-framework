package de.fearnixx.t3.event.bot;

import de.fearnixx.t3.IBot;
import de.fearnixx.t3.event.IEvent;

/**
 * Created by MarkL4YG on 29-Jan-18
 */
public interface IBotEvent extends IEvent {

    /**
     * Returns the bot from which the event originated
     */
    IBot getBot();
}
