package de.fearnixx.t3.event.state;

import de.fearnixx.t3.event.BotEvent;
import de.fearnixx.t3.IT3Bot;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public class BotStateEvent extends BotEvent implements IBotStateEvent {


    public BotStateEvent(IT3Bot t3bot) {
        super(t3bot);
    }

    public static class PluginsLoaded extends BotStateEvent implements IPluginsLoaded {
        public PluginsLoaded(IT3Bot t3bot) {
            super(t3bot);
        }
    }

    public static class Initialize extends BotStateEvent implements IInitializeEvent {

        private boolean canceled = false;

        public Initialize(IT3Bot t3bot) {
            super(t3bot);
        }

        public void cancel() {
            canceled = true;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }

    public static class PreConnect extends BotStateEvent implements IPreConnect {
        public PreConnect(IT3Bot t3bot) {
            super(t3bot);
        }
    }

    public static class PostConnect extends BotStateEvent implements IPostConnect {
        public PostConnect(IT3Bot t3bot) {
            super(t3bot);
        }
    }

    public static class PreShutdown extends BotStateEvent implements IPreShutdown {
        public PreShutdown(IT3Bot t3bot) {
            super(t3bot);
        }
    }

    public static class PostShutdown extends BotStateEvent implements IPostShutdown {
        public PostShutdown(IT3Bot t3bot) {
            super(t3bot);
        }
    }
}
