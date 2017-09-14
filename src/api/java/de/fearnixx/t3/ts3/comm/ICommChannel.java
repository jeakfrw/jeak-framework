package de.fearnixx.t3.ts3.comm;

import de.fearnixx.t3.ts3.comm.except.CommException;
import de.fearnixx.t3.ts3.keys.TargetType;

/**
 * Created by Life4YourGames on 05.07.17.
 *
 * Abstract representation to allow communication with clients using the private, channel or server chat
 *
 *  The concept is conversation-based which means that once a conversation started all others have to wait!
 *  @see #openWith(ICommHandle) and
 *  @see #closeWith(ICommHandle) for further detail
 *
 *  @implNote Thread-safety is ensured
 */
public interface ICommChannel {


    /**
     * Target ID: Client for private, channel for channel and 0 for server -ID chat
     * @return The targets ID
     */
    int getTargetID();

    /**
     * Determines the targets type of the CommChannel
     * Client/Private, Channel/Channel, Server/Server
     * @see #getTargetID() for association
     * @return The target type
     */
    TargetType getTargetType();

    /**
     * Start a new conversation using the provided handle
     *
     * Starting a conversation blocks all other handles to be queued instead of sending!
     * This ensures that conversations don't cross each-other
     *
     * @see #sendMessage(String) for some special per-Thread rules!
     * @param h The Communication Handle to use
     * @return If the claim has been successful - If the handle now blocks others
     */
    boolean openWith(ICommHandle h);

    /**
     * Close the active conversation
     * Only closes if the handle currently blocks others (owns the conversation)
     *
     * @param h The Communication Handle to use
     * @return If the current conversation is closed - OR if there's no communication open
     */
    boolean closeWith(ICommHandle h);

    /**
     * Send a new message
     *
     * Since there's no handle here take note of the following:
     * @implNote When no handle has been provided a check for if the threads are the same is performed
     * if true the message is sent. Beware: Event listeners share the same thread! So avoid using this in listeners if you're unsure
     * @param msg The message to send
     */
    void sendMessage(String msg);

    /**
     * Send a message if the handle is currently owning the conversation.
     * Queues the message if not
     * @param h The Communication Handle to use
     * @param msg The Message to send
     */
    void sendMessage(ICommHandle h, String msg);

    /**
     * Same as {@link #sendMessage(ICommHandle, String)} just that this method does NOT return until the message is sent
     *
     * @param h The Communication Handle to use
     * @param msg The message to send
     * @throws CommException.Closed -
     *   When the channel is closed before the message is sent
     */
    void sendMessageBlocking(ICommHandle h, String msg) throws CommException.Closed;
}
