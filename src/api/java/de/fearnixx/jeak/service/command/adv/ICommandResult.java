package de.fearnixx.jeak.service.command.adv;

public interface ICommandResult {

    /**
     * The message to be sent to the command issuer to notify about the result.
     */
    String getMessage();

    /**
     * Indicates whether or not the command executed successfully.
     */
    default boolean wasFailure() {
        return false;
    }

    /**
     * When false, the next subsequent command executor (determined by the reverse order of registration) will be receiving the command too.
     * Plugins deliberately overwriting existing commands can use this to cause the overwritten executor to be executed too.
     */
    default boolean wasConsumed() {
        return true;
    }
}
