package de.fearnixx.t3.event.bot;

import de.fearnixx.t3.IBot;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public class BotStateEvent extends BotEvent implements IBotStateEvent {

    public static class PluginsLoaded extends BotStateEvent implements IPluginsLoaded {
    }

    public static class Initialize extends BotStateEvent implements IInitializeEvent {

        private boolean canceled = false;

        public void cancel() {
            canceled = true;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }

    public static class PreConnect extends BotStateEvent implements IPreConnect {
    }

    public static class PostConnect extends BotStateEvent implements IPostConnect {
    }

    public static class PreShutdown extends BotStateEvent implements IPreShutdown {
    }

    public static class PostShutdown extends BotStateEvent implements IPostShutdown {
    }
}
