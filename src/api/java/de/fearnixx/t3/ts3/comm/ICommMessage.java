package de.fearnixx.t3.ts3.comm;

import de.fearnixx.t3.ts3.keys.TargetType;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public interface ICommMessage {

    TargetType getSourceType();
    int getSourceID();
    String getSourceNickName();
    String getSourceUID();

    String getMessage();
}
