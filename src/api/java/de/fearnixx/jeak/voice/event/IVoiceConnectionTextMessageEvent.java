package de.fearnixx.jeak.voice.event;

/**
 * Represents the event that occurs when a voice connection receives a text message
 */
public interface IVoiceConnectionTextMessageEvent extends IVoiceConnectionEvent {

    /**
     * @return the invoker uid of the message sent to the voice connection
     */
    String getInvokerUid();

    /**
     * @return the message sent to the voice connection
     */
    String getMessage();

    /**
     * @return the source id of the message
     */
    Integer getSourceId();

    /**
     * @return the target type of the message
     */
    Integer getTargetType();
}
