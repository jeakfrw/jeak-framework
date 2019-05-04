package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.notification.*;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import java.util.Optional;

@JeakBotPlugin(id = "notificationtest")
public class NotificationTestPlugin extends AbstractTestPlugin {

    private static final String RECIPIENT_UID = "GANC6dTbew+a3A2h/8c5CGJXzsE=";

    @Inject
    private INotificationService notificationService;

    @Inject
    private IProfileService profileService;

    public NotificationTestPlugin() {
        super();
        addTest("notficiationTest_profileRetrieved");
        addTest("notficiationTest_messageDispatched");
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect events) {
        Optional<IUserProfile> optProfile = profileService.getOrCreateProfile(RECIPIENT_UID);
        if (!optProfile.isPresent()) {
            fail("notficiationTest_profileRetrieved");
            return;
        }

        optProfile.get().setOption(SendMailChannel.MAIL_ADDRESS_PROFILE_OPTION, "technik@fearnixx.de");
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
