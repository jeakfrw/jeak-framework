package de.fearnixx.jeak.teamspeak.query.api;

import de.fearnixx.jeak.event.query.RawQueryEvent;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.nio.channels.Channel;
import java.util.function.Consumer;

/**
 * Abstraction layer of communication channels connected to a TeamSpeak query interface.
 *
 * @author Magnus Leßmann
 * @since 1.2.0
 */
public interface ITSMessageChannel extends Channel, Runnable {

    /**
     * Whether or not this channel is generally ready to send requests.
     *
     * @apiNote This only determines the readiness based on the greeting. See {@link #hasPendingRequest()} too.
     */
    boolean isReady();

    /**
     * Whether or not there's currently a request waiting for an answer.
     *
     * @see #writeMessage(IQueryRequest)
     */
    boolean hasPendingRequest();

    /**
     * Attempts to write a message to this channel.
     * The message is kept pending until an answer has been received or the request is rejected due to an exception (see: {@link #setRejectedMessageCallback(Consumer)}.
     *
     * @throws IllegalStateException When the channel is not ready to send messages yet. (Unreceived greeting or still a pending request.)
     */
    void writeMessage(IQueryRequest message);

    /**
     * Sets the listener to be notified of an answer when one is received.
     */
    void setAnswerCallback(Consumer<RawQueryEvent.Message.Answer> answerConsumer);

    /**
     * Sets the listener to be notified of a notification when one is received.
     */
    void setNotificationCallback(Consumer<RawQueryEvent.Message.Notification> notificationConsumer);

    /**
     * Sets the listener to be notified of message rejection.
     *
     * @implNote This is only fired when the request could not be sent through the underlying channel because of an exception.
     */
    void setRejectedMessageCallback(Consumer<IQueryRequest> rejectedMessageConsumer);
}
