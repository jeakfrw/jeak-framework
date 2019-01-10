package de.fearnixx.jeak.event.bot;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.JeakBot;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public class BotEvent implements IBotEvent {

    private IBot bot;

    public BotEvent setBot(JeakBot bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public IBot getBot() {
        return bot;
    }
}