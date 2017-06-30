package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.query.IQueryMessageObject;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public interface IDBClient extends IQueryMessageObject {

    boolean isValid();

    int getDBID();

    String getUniqueID();

    String getUniqueBase64ID();

    String getNickName();

    String getDescription();

    String getIconID();

    String getAvatarID();

    long getCreated();

    LocalDateTime getCreatedTime();

    long getLastJoin();

    LocalDateTime getLastJoinTime();

    InetAddress getLastAddress();

    int getConnectionCount();

    long getBytesDownloadedMonth();

    long getBytesDownloadedTotal();

    long getBytesUploadedMonth();

    long getBytesUploadedTotal();
}
