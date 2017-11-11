package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.ts3.chat.IChatMessage;
import de.fearnixx.t3.ts3.keys.NotificationType;
import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.keys.TargetType;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public class QueryNotification extends QueryMessage implements IQueryNotification {

    private NotificationType type;

    protected void setType(NotificationType type) {
        this.type = type;
    }

    @Override
    public NotificationType getNotificationType() {
        return type;
    }

    public static class ClientEnterView extends QueryNotification implements IClientEnterView {
        public ClientEnterView() {
            super.setType(NotificationType.CLIENT_ENTER);
        }
    }

    public static class ClientLeaveView extends QueryNotification implements IClientLeaveView {
        public ClientLeaveView() {
            super.setType(NotificationType.CLIENT_LEAVE);
        }
    }

    public static class ClientMoved extends QueryNotification implements IClientMoved {
        public ClientMoved() {
            super.setType(NotificationType.CLIENT_MOVED);
        }
    }

    public static class TextMessage extends QueryNotification implements ITextMessage {

        private static final NotificationType[] types =
                {NotificationType.TEXT_PRIVATE, NotificationType.TEXT_CHANNEL, NotificationType.TEXT_SERVER};

        protected void createTextMessage(TargetType sourceType) {
            super.setType(types[sourceType.ordinal()]);
        }

        @Override
        public IChatMessage getChatMessage() {
            return ((IChatMessage) getRawObjects().get(0));
        }
    }
}
