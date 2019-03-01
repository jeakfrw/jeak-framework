package de.fearnixx.jeak.event.bot;

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

    public static class ConnectEvent  extends BotStateEvent {

        public static class PreConnect extends ConnectEvent implements IConnectStateEvent.IPreConnect {
        }

        public static class PostConnect extends ConnectEvent implements IConnectStateEvent.IPostConnect {
        }

        public static class Disconnect extends ConnectEvent implements IConnectStateEvent.IDisconnect {

            private boolean graceful = false;

            public Disconnect(boolean graceful) {
                this.graceful = graceful;
            }

            @Override
            public boolean isGraceful() {
                return graceful;
            }
        }

        public static class ConnectFailed extends ConnectEvent implements IConnectStateEvent.IConnectFailed {

            private int attempts;
            private int maxAttempts;

            public void setAttempts(int attempts) {
                this.attempts = attempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            @Override
            public int attemptCount() {
                return attempts;
            }

            @Override
            public int maxAttempts() {
                return maxAttempts;
            }
        }
    }

    public static class PreShutdown extends BotStateEvent implements IPreShutdown {
    }

    public static class PostShutdown extends BotStateEvent implements IPostShutdown {
    }

    public static class PreInitializeEvent extends BotStateEvent implements IPreInitializeEvent {
    }
}
