package de.fearnixx.jeak.event.bot;

import java.util.concurrent.ExecutorService;

/**
 * Event classes representing run state changes.
 */
public interface IBotStateEvent extends IBotEvent {

    /**
     * ## Phase 1 ##.
     * Plugins are now loaded
     * (Early-Initialization)
     */
    interface IPluginsLoaded extends IBotStateEvent {

    }

    /**
     * ## Phase 2 ##.
     * Bot completed early-initialization.
     * Plugins should now pre-initialize:
     * * Create Managers (with injections but no semantic initialization)
     * * Register services (so that injections can correctly work)
     *
     * <p>This phase is not designed for IO or similar operations.
     * Thus, it is synchronized, not cancellable and will ungracefully shut down the bot upon any exception!
     * (Keep in mind that 'the bot' does not necessarily correspond to the full JVM application!)
     */
    interface IPreInitializeEvent extends IBotStateEvent {

    }

    /**
     * ## Phase 3 ##.
     * Plugins may now initialize configuration, semantics, load dependencies and whatnot.
     *
     * @see #cancel() especially.
     */
    interface IInitializeEvent extends IBotStateEvent {

        /**
         * Cancels the Start-Up.
         * Keep in mind that the event execution is not interrupted!
         * This indicates a critical plugin ore service could not successfully initialize
         * but does not require ungraceful shut-down.
         *
         * <p>Suitable for example for first-time setups, unsatisfying configuration or similar things.
         */
        void cancel();

        /**
         * Whether or not something already invoked {@link #cancel()}.
         *
         * <p>Primarily available for information purposes
         * but if you want to cancel your own initialization based on this, you can do so.
         */
        boolean isCanceled();
    }

    /**
     * Child interfaces of this event can be fired multiple times during the application lifecycle.
     * This is due to reconnect-attempts when the connection has been lost.
     */
    interface IConnectStateEvent extends IBotStateEvent {

        /**
         * ## Phase 4 ##.
         * The Bot is about to establish the main query connection.
         * @see IConnectFailed for when the attempt failed.
         */
        interface IPreConnect extends IConnectStateEvent {

        }

        /**
         * ## Phase 5 ##.
         * The Bot has established the main query connection successfully
         * The following commands have been executed:
         * * use
         * * login
         * * clientupdate nickname
         *
         * <p>Plugins may now spawn additional connections to the server.
         * (As now credentials and nickname have been validated)
         */
        interface IPostConnect extends IConnectStateEvent {

        }

        /**
         * ## Intermediate phase ##.
         * Application lifecycle will do either:
         * <ul>
         *     <li>Roll back to phase 4 for reconnection attempts.</li>
         *     <li>Continue to {@link IPreShutdown} when no re-connection attempt shall be made.</li>
         * </ul>
         * @implNote Will fire only once per connection lost.
         */
        interface IDisconnect extends IConnectStateEvent {

            boolean isGraceful();
        }

        /**
         * ## Intermediate phase ##.
         * Application lifecycle will do either:
         * <ul>
         *     <li>Roll back to phase 4 ({@link IPreConnect}) for connection attempts.</li>
         *     <li>Continue to {@link IPreShutdown} when max connection attempts has been reached.</li>
         * </ul>
         */
        interface IConnectFailed extends IConnectStateEvent {

            int attemptCount();

            int maxAttempts();
        }
    }

    /**
     * ## Phase 6 ##
     * The Bot is about to shut down
     * The main connection is about to close
     *
     * <p>Please start terminating non-vital asynchronous threads such as other connections.
     * @implNote may be followed by a graceful {@link IConnectStateEvent.IDisconnect} when the connection is alive.
     */
    interface IPreShutdown extends IBotStateEvent {

        /**
         * Register executors to the event consumer.
         * The framework will call {@link ExecutorService#shutdownNow()} after the shutdown sleep period.
         */
        void addExecutor(ExecutorService executorService);
    }

    /**
     * ## Phase 7 ##
     * Shutdown completed the bot (not necessarily the application!) is about to exit.
     *
     * <p>Terminate all asynchronous threads!
     */
    interface IPostShutdown extends IBotStateEvent {

        /**
         * @see IPreShutdown#addExecutor(ExecutorService)
         */
        void addExecutor(ExecutorService executorService);
    }
}
