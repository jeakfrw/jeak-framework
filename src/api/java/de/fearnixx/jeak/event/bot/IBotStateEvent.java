package de.fearnixx.jeak.event.bot;

/**
 * Event classes representing run state changes.
 *
 * @author MarkL4YG
 */
public interface IBotStateEvent extends IBotEvent {

    /**
     * ## Phase 1 ##
     * Plugins are now loaded
     * (Early-Initialization)
     */
    interface IPluginsLoaded extends IBotStateEvent {

    }

    /**
     * ## Phase 2 ##
     * Bot completed early-initialization.
     * Plugins should now pre-initialize:
     * * Create Managers (with injections but no semantic initialization)
     * * Register services (so that injections can correctly work)
     *
     * This phase is not designed for IO or similar operations.
     * Thus, it is synchronized, not cancellable and will ungracefully shut down the bot upon any exception!
     * (Keep in mind that 'the bot' does not necessarily correspond to the full JVM application!)
     */
    interface IPreInitializeEvent extends IBotStateEvent {

    }

    /**
     * ## Phase 3 ##
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
         * Suitable for example for first-time setups, unsatisfying configuration or similar things.
         */
        void cancel();

        /**
         * Whether or not something already invoked {@link #cancel()}.
         *
         * Primarily available for information purposes
         * but if you want to cancel your own initialization based on this, you can do so.
         */
        boolean isCanceled();
    }

    /**
     * ## Phase 4 ##
     * The Bot is about to establish the main query connection.
     * Plugins may also start initializing external connections such as pinging APIs or similar actions.
     * Please note that all persistent connections MUST be:
     * * asynchronous!
     * * closed at {@link IPostShutdown} if not earlier!
     * * responding to interrupts!
     */
    interface IPreConnect extends IBotStateEvent {

    }

    /**
     * ## Phase 5 ##
     * The Bot has established the main query connection successfully
     * The following commands have been executed:
     * * use
     * * login
     * * clientupdate nickname
     *
     * Plugins may now spawn additional connections to the server.
     * (As now credentials and nickname have been validated)
     */
    interface IPostConnect extends IBotStateEvent {

    }

    /**
     * ## Phase 6 ##
     * The Bot is about to shut down
     * The main connection is about to close
     *
     * Please start terminating non-vital asynchronous threads such as other connections.
     */
    interface IPreShutdown extends IBotStateEvent {

    }

    /**
     * ## Phase 7 ##
     * Shutdown completed the bot (not necessarily the application!) is about to exit.
     *
     * Terminate all asynchronous threads!
     */
    interface IPostShutdown extends IBotStateEvent {

    }
}
