package de.fearnixx.t3.ts3.client;

import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.query.QueryMessageObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by MarkL4YG on 30.06.17.
 */
@SuppressWarnings("ConstantConditions")
public class TS3DBClient extends QueryMessageObject implements IDBClient {

    private boolean invalidated = false;

    public void invalidate() {
        synchronized (super.lock) {
            invalidated = true;
        }
    }

    @Override
    public Boolean isValid() {
        synchronized (super.lock) {
            return !invalidated;
        }
    }

    @Override
    public Integer getDBID() {
        return Integer.parseInt(getProperty(PropertyKeys.DBClient.DBID).get());
    }

    @Override
    public String getUniqueID() {
        return getProperty(PropertyKeys.DBClient.UID).get();
    }

    @Override
    public String getUniqueBase64ID() {
        return getProperty(PropertyKeys.DBClient.UID64).get();
    }

    @Override
    public String getNickName() {
        return getProperty(PropertyKeys.DBClient.NICKNAME).get();
    }

    @Override
    public String getDescription() {
        return getProperty(PropertyKeys.DBClient.DESCRIPTION).get();
    }

    @Override
    public String getIconID() {
        return getProperty(PropertyKeys.DBClient.ICON_ID).get();
    }

    @Override
    public String getAvatarID() {
        return getProperty(PropertyKeys.DBClient.AVATAR).get();
    }

    @Override
    public Long getCreated() {
        return Long.parseLong(getProperty(PropertyKeys.DBClient.CREATED_TIME).get());
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return LocalDateTime.ofEpochSecond(getCreated(), 0, ZoneOffset.UTC);
    }

    @Override
    public Long getLastJoin() {
        return Long.parseLong(getProperty(PropertyKeys.DBClient.LAST_JOIN_TIME).get());
    }

    @Override
    public LocalDateTime getLastJoinTime() {
        return LocalDateTime.ofEpochSecond(getLastJoin(), 0, ZoneOffset.UTC);
    }

    @Override
    public InetAddress getLastAddress() {
        try {
            return InetAddress.getByName(getProperty(PropertyKeys.DBClient.LAST_IP).get());
        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid LAST_IP returned!", e);
        }
    }

    @Override
    public Integer getConnectionCount() {
        return Integer.parseInt(getProperty(PropertyKeys.DBClient.TOTAL_CONNECTIONS).get());
    }

    @Override
    public Long getBytesDownloadedMonth() {
        return Long.parseLong(getProperty(PropertyKeys.DBClient.DOWNLOAD_MONTH).get());
    }

    @Override
    public Long getBytesDownloadedTotal() {
        return Long.parseLong(getProperty(PropertyKeys.DBClient.DOWNLOAD_TOTAL).get());
    }

    @Override
    public Long getBytesUploadedMonth() {
        return Long.parseLong(getProperty(PropertyKeys.DBClient.UPLOAD_MONTH).get());
    }

    @Override
    public Long getBytesUploadedTotal() {
        return Long.parseLong(getProperty(PropertyKeys.DBClient.UPLOAD_TOTAL).get());
    }
}
