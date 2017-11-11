package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.ts3.chat.IChatMessage;
import de.fearnixx.t3.ts3.keys.NotificationType;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public interface IQueryNotification extends IQueryMessage {

    interface IClientEnterView {}

    interface IClientLeaveView {}

    interface IClientMoved {}

    interface ITextMessage extends IQueryNotification {

        IChatMessage getChatMessage();
    }

    NotificationType getNotificationType();
}
