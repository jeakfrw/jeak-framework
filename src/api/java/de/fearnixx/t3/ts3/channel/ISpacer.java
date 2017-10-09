package de.fearnixx.t3.ts3.channel;

/**
 * Created by MarkL4YG on 15.06.17.
 *
 * Abstract representation of a spacer using the information queried via the ServerQuery
 * This extends a normal Channel representation:
 * @see IChannel
 *
 * @apiNote For information on how to write properties see {@link IChannel}
 *
 * @implNote For information about thread-safety see {@link IChannel}
 */
public interface ISpacer extends IChannel {

    /**
     * @return The name with the spacer prefix stripped off
     */
    String getStrippedName();

    /**
     * @return The extracted spacer number
     * @see IChannel#isSpacer()
     */
    Float getNumber();

    /**
     * Checks if the spacer has the "centered" flag
     * "[cspacer]"
     * @return If the center flag exists
     */
    Boolean isCentered();

    /**
     * Checks if the spacer has the "repeat" flag
     * "[*spacer]"
     * @return If the repeat flag exists
     */
    Boolean isRepeated();
}
