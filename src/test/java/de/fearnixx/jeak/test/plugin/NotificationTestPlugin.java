package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.notification.*;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@JeakBotPlugin(id = "notificationtest")
public class NotificationTestPlugin extends AbstractTestPlugin {

    private static final String RECIPIENT_UID = "GANC6dTbew+a3A2h/8c5CGJXzsE=";
    private static final String PROP_TEST_RECIPIENT = "jeak.test.notifyTest.recipient";
    private static final String NOTIFY_TEST_RECIPIENT = Main.getProperty(PROP_TEST_RECIPIENT, null);

    private static final Logger logger = LoggerFactory.getLogger(NotificationTestPlugin.class);

    @Inject
    private INotificationService notificationService;

    @Inject
    private IProfileService profileService;

    public NotificationTestPlugin() {
        super();
        addTest("notificationTest_liveMessageDispatched");

        if (NOTIFY_TEST_RECIPIENT != null) {
            addTest("notficiationTest_profileRetrieved");
            addTest("notficiationTest_messageDispatched");
        } else {
            logger.warn("Not running mail tests as no recipient has been defined. ({})", PROP_TEST_RECIPIENT);
        }
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect events) {
        if (NOTIFY_TEST_RECIPIENT != null) {
            Optional<IUserProfile> optProfile = profileService.getOrCreateProfile(RECIPIENT_UID);
            if (!optProfile.isPresent()) {
                fail("notficiationTest_profileRetrieved");
                return;
            }

            optProfile.get().setOption(SendMailChannel.MAIL_ADDRESS_PROFILE_OPTION, PROP_TEST_RECIPIENT);
            INotification notification = INotification.builder()
                    .addRecipient(RECIPIENT_UID)
                    .urgency(Urgency.ALERT)
                    .lifespan(Lifespan.FOREVER)
                    .summary("[TESTSYSTEM] Mail notification test.")
                    .longText("This is a test notification.")
                    .build();
            notificationService.dispatch(notification);
            success("notficiationTest_messageDispatched");
        }
    }

    @Listener
    public void onUserJoined(IQueryEvent.INotification.IClientEnter event) {
        Optional<String> optUUID = event.getProperty(PropertyKeys.Client.UID);

        optUUID.ifPresent(uid ->  {
            if (uid.equals(RECIPIENT_UID)) {
                INotification notification = INotification.builder()
                        .addRecipient(RECIPIENT_UID)
                        .urgency(Urgency.BASIC)
                        .lifespan(Lifespan.BASIC)
                        .summary("[TESTSYSTEM] TS3 notification test.")
                        .shortText("This is a test notification.")
                        .build();
                notificationService.dispatch(notification);
                success("notificationTest_liveMessageDispatched");
            }
        });
    }
}
