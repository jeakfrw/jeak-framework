package de.fearnixx.jeak.event.bot;

import de.fearnixx.jeak.IBot;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public class BotEvent implements IBotEvent {

    private IBot bot;

    public BotEvent setBot(IBot bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public IBot getBot() {
        return bot;
    }
}
