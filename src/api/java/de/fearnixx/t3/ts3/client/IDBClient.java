package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.ts3.query.IQueryMessageObject;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public interface IDBClient extends IQueryMessageObject {

    Boolean isValid();

    Integer getDBID();

    String getUniqueID();

    String getUniqueBase64ID();

    String getNickName();

    String getDescription();

    String getIconID();

    String getAvatarID();

    Long getCreated();

    LocalDateTime getCreatedTime();

    Long getLastJoin();

    LocalDateTime getLastJoinTime();

    InetAddress getLastAddress();

    Integer getConnectionCount();

    Long getBytesDownloadedMonth();

    Long getBytesDownloadedTotal();

    Long getBytesUploadedMonth();

    Long getBytesUploadedTotal();
}
