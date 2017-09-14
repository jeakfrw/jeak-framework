package de.fearnixx.t3.event;

import de.fearnixx.t3.IT3Bot;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IEvent {

    interface IBotEvent extends IEvent {

        IT3Bot getBot();
    }
}
