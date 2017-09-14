package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.ts3.comm.ICommMessage;
import de.fearnixx.t3.ts3.keys.NotificationType;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public interface IQueryNotification extends IQueryMessage {

    interface IClientEnterView {}

    interface IClientLeaveView {}

    interface IText extends IQueryNotification{

        ICommMessage getTextMessage();
    }

    NotificationType getNotificationType();
}
