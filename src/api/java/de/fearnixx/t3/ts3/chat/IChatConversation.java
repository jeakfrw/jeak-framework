package de.fearnixx.t3.ts3.chat;

import de.fearnixx.t3.ts3.keys.TargetType;

/**
 * Created by MarkL4YG on 11-Nov-17
 */
public interface IChatConversation {

    Integer getTargetID();

    TargetType getTargetType();

    boolean openWith(IChatHandle handle);

    boolean closeWith(IChatHandle handle);

    /**
     * Send a message to the conversation - Bypasses all handles!
     * This is intended for announcements only...
     *
     * @param message The message to send
     * @deprecated Please avoid using this. This method ignores all handle-functionality!
     */
    @Deprecated
    void sendMessage(String message);

    /**
     * Queues a message to be sent when the specified handle acquires the lock
     *
     * @param handle The handle
     * @param message The message
     */
    void sendMessage(IChatHandle handle, String message);
}
