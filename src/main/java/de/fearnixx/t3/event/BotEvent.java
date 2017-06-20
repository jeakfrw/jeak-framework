package de.fearnixx.t3.event;

import de.fearnixx.t3.IT3Bot;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public class BotEvent implements IEvent.IBotEvent {

    private IT3Bot t3bot;

    public BotEvent(IT3Bot t3bot) {
        this.t3bot = t3bot;
    }

    @Override
    public IT3Bot getBot() {
        return t3bot;
    }
}
