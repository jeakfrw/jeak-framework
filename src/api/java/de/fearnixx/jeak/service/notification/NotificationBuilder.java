package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.teamspeak.data.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Builds a new notification.
 * For information, see the notification interface: {@link INotification}
 */
public class NotificationBuilder {

    private static final int TS3_MAX_TEXTMSG_LENGTH = 1024;

    private static final Logger logger = LoggerFactory.getLogger(NotificationBuilder.class);

    private List<String> recipients;
    private String summary;
    private String shortText;
    private String longText;
    private int urgency;
    private int lifespan;

    public NotificationBuilder() {
        reset();
    }

    public NotificationBuilder addRecipient(IClient client) {
        Objects.requireNonNull(client, "Recipient may not be null!");
        return addRecipient(client.getClientUniqueID());
    }

    public NotificationBuilder addRecipient(String uuid) {
        Objects.requireNonNull(uuid, "Unique id may not be null!");
        recipients.add(uuid);
        return this;
    }

    public NotificationBuilder shortText(String shortText) {
        Objects.requireNonNull(shortText, "Short text may not be null!");

        if (shortText.length() > TS3_MAX_TEXTMSG_LENGTH) {
            logger.warn("Someone set a short text longer than the allowed TS3 text message length. This is not recommended!");
        }

        this.shortText = shortText;
        return this;
    }

    public NotificationBuilder longText(String longText) {
        this.longText = longText;
        return this;
    }

    public NotificationBuilder urgency(Urgency urgency) {
        return urgency(urgency.getLevel());
    }

    public NotificationBuilder urgency(int urgency) {
        this.urgency = urgency;
        return this;
    }

    public NotificationBuilder lifespan(Lifespan lifespan) {
        return lifespan(lifespan.getLevel());
    }

    public NotificationBuilder lifespan(int lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    /**
     *
     * suppresses: squid:HiddenFieldCheck
     *   - Since <strong>all</strong> fields are hidden to construct the anonymous class.
     */
    @SuppressWarnings("squid:HiddenFieldCheck")
    public INotification build() {
        boolean hasLongText = longText != null && !longText.isEmpty();
        final String summary = this.summary;
        final String shortText = this.shortText;
        final String longText = hasLongText ? this.longText : this.shortText;
        final int urgency = this.urgency;
        final int lifespan = this.lifespan;
        final List<String> recipients =
                Collections.unmodifiableList(new ArrayList<>(this.recipients));

        return new INotification() {
            @Override
            public List<String> getRecipients() {
                return recipients;
            }

            @Override
            public int getUrgency() {
                return urgency;
            }

            @Override
            public int getLifespan() {
                return lifespan;
            }

            @Override
            public String getSummary() {
                return summary;
            }

            @Override
            public String getShortText() {
                return shortText;
            }

            @Override
            public String getLongText() {
                return longText;
            }
        };
    }

    /**
     * Resets this builder to a fresh state so it can be fully re-used if needed.
     */
    public NotificationBuilder reset() {
        recipients = new LinkedList<>();
        summary = "No summary provided.";
        shortText = "No short text provided.";
        longText = null;
        urgency = Urgency.BASIC.getLevel();
        lifespan = Lifespan.BASIC.getLevel();
        return this;
    }
}
