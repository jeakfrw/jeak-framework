package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.ts3.keys.NotificationType;
import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.keys.TargetType;
import de.fearnixx.t3.ts3.comm.CommMessage;

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

    public static class Text extends QueryNotification implements IText {

        private static final NotificationType[] types =
                {NotificationType.TEXT_PRIVATE, NotificationType.TEXT_CHANNEL, NotificationType.TEXT_SERVER};
        private CommMessage textMessage;

        protected void createTextMessage(TargetType sourceType) {
            super.setType(types[sourceType.ordinal()]);
            IQueryMessageObject o = getRawObjects().get(0);
            textMessage = new CommMessage(
                    sourceType,
                    o.getProperty(PropertyKeys.TextMessage.SOURCE_UID).get(),
                    Integer.parseInt(o.getProperty(PropertyKeys.TextMessage.SOURCE_ID).get()),
                    o.getProperty(PropertyKeys.TextMessage.SOURCE_NICKNAME).get(),
                    o.getProperty(PropertyKeys.TextMessage.MESSAGE).get()
            );
        }

        @Override
        public CommMessage getTextMessage() {
            return textMessage;
        }
    }
}
