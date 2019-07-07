package de.fearnixx.jeak.event.bot;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

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

            private final boolean graceful;

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

        private final List<ExecutorService> executors = new LinkedList<>();

        @Override
        public void addExecutor(ExecutorService executorService) {
            Objects.requireNonNull(executorService, "Executor service may not be null!");
            executors.add(executorService);
        }

        public List<ExecutorService> getExecutors() {
            return executors;
        }
    }

    public static class PostShutdown extends BotStateEvent implements IPostShutdown {

        private final List<ExecutorService> executors = new LinkedList<>();

        @Override
        public void addExecutor(ExecutorService executorService) {
            Objects.requireNonNull(executorService, "Executor service may not be null!");
            executors.add(executorService);
        }

        public List<ExecutorService> getExecutors() {
            return executors;
        }
    }

    public static class PreInitializeEvent extends BotStateEvent implements IPreInitializeEvent {
    }
}
