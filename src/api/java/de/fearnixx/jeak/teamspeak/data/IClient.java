package de.fearnixx.jeak.teamspeak.data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by MarkL4YG on 20.06.17.
 *
 * Abstract representation of online clients
 */
public interface IClient extends IDataHolder {

    Boolean isValid();

    /**
     * @return The clients ID
     * @apiNote This is only valid while the client is online. Refer to {@link #getClientDBID()}
     */
    Integer getClientID();

    /**
     * @return The database ID of this client
     */
    Integer getClientDBID();

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
        VOICE,
        QUERY
    }
    ClientType getClientType();

    Integer getChannelID();

    Integer getChannelGroupID();

    Integer getChannelGroupSource();

    Boolean isAway();

    String getAwayMessage();

    Integer getTalkPower();

    Boolean isTalking();

    Boolean isTalker();

    Boolean isPrioTalker();

    Boolean isCommander();

    Boolean isRecording();

    Boolean hasMic();

    Boolean hasMicMuted();

    Boolean hasOutput();

    Boolean hasOutputMuted();

    List<Integer> getGroupIDs();

    Integer getIdleTime();

    Long getCreated();

    LocalDateTime getCreatedTime();

    Long getLastJoin();

    LocalDateTime getLastJoinTime();
}
