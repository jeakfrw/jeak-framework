package de.fearnixx.t3.event.bot;

import de.fearnixx.t3.IBot;
import de.fearnixx.t3.T3Bot;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public class BotEvent implements IBotEvent {

    private IBot t3bot;

    public BotEvent setBot(T3Bot bot) {
        this.t3bot = bot;
        return this;
    }

    @Override
    public IBot getBot() {
        return t3bot;
    }
}
