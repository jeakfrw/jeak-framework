package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.ts3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.comm.ICommChannel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by MarkL4YG on 20.06.17.
 *
 * Abstract representation of online clients
 */
public interface IClient extends IQueryMessageObject {

    boolean isValid();

    /**
     * @return The clients ID
     * @apiNote This is only valid while the client is online. Refer to {@link #getClientDBID()}
     */
    int getClientID();

    /**
     * @return The database ID of this client
     */
    int getClientDBID();

    /**
     * @return The unique client identifier
     */
    String getClientUniqueID();

    /**
     * @return The clients nick name
     */
    String getNickName();

    String getIconID();

    enum PlatformType {
        UNKNOWN,
        WINDOWS,
        LINUX,
        ANDROID,
        OSX,
        IOS
    }
    PlatformType getPlatform();

    String getVersion();

    enum ClientType {
        QUERY,
        VOICE
    }
    ClientType getClientType();

    int getChannelID();

    int getChannelGroupID();

    int getChannelGroupSource();

    boolean isAway();

    String getAwayMessage();

    int getTalkPower();

    boolean isTalking();

    boolean isTalker();

    boolean isPrioTalker();

    boolean isCommander();

    boolean isRecording();

    boolean hasMic();

    boolean hasMicMuted();

    boolean hasOutput();

    boolean hasOutputMuted();

    List<Integer> getGroupIDs();

    int getIdleTime();

    long getCreated();

    LocalDateTime getCreatedTime();

    long getLastJoin();

    LocalDateTime getLastJoinTime();
}
