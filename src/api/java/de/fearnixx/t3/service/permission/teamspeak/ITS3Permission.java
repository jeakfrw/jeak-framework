package de.fearnixx.t3.service.permission.teamspeak;

import de.fearnixx.t3.service.permission.base.IPermission;

/**
 * Created by MarkL4YG on 04-Feb-18
 */
public interface ITS3Permission extends IPermission {

    Boolean getSkip();

    Boolean getNegate();

    PriorityType getPriorityType();

    enum PriorityType {

        CHANNEL_CLIENT,
        CLIENT,
        CHANNEL_GROUP,
        CHANNEL,
        SERVER_GROUP
    }
}
