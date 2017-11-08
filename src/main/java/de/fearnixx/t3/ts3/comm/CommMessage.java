package de.fearnixx.t3.ts3.comm;

import de.fearnixx.t3.ts3.comm.ICommMessage;
import de.fearnixx.t3.ts3.keys.TargetType;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public class CommMessage implements ICommMessage {

    final TargetType sourceType;
    final String sourceUID;
    final int sourceID;
    final String sourceNick;
    final String message;

    public CommMessage(
            TargetType sourceType,
            String sourceUID,
            int sourceID,
            String sourceNick,
            String message) {
        this.sourceType = sourceType;
        this.sourceUID = sourceUID;
        this.sourceID = sourceID;
        this.sourceNick = sourceNick;
        this.message = message;
    }

    @Override
    public TargetType getSourceType() {
        return sourceType;
    }

    @Override
    public String getSourceUID() {
        return sourceUID;
    }

    @Override
    public Integer getSourceID() {
        return sourceID;
    }

    @Override
    public String getSourceNickName() {
        return sourceNick;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
