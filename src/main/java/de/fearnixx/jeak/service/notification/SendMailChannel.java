package de.fearnixx.jeak.service.notification;

import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.TransportUnit;
import de.fearnixx.jeak.service.mail.IMail;
import de.fearnixx.jeak.service.mail.ITransportUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SendMailChannel implements INotificationChannel {

    public static final String MAIL_ADDRESS_PROFILE_OPTION = "notifications:email";
    private static final Logger logger = LoggerFactory.getLogger(SendMailChannel.class);

    @Inject
    @TransportUnit(name = "mail-notifications")
    private ITransportUnit transportUnit;

    @Inject
    private IProfileService profileService;

    @Override
    public int lowestUrgency() {
        return Urgency.BASIC.getLevel();
    }

    @Override
    public int highestUrgency() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int lowestLifespan() {
        return Lifespan.SHORTER.getLevel();
    }

    @Override
    public int highestLifespan() {
        return Lifespan.FOREVER.getLevel();
    }

    @Override
    public void sendNotification(INotification notification) {
        if (transportUnit == null) {
            logger.warn("Cannot send mail notifications! Transport unit \"mail-notifications\" is not registered!");
            return;
        }

        List<String> mailReceivers = new LinkedList<>();
        notification.getRecipients().forEach(recipient -> {
            Optional<IUserProfile> optProfile = profileService.getProfile(recipient);

            if (optProfile.isEmpty()) {
                if (notification.getUrgency() >= Urgency.WARN.getLevel()) {
                    logger.warn("Cannot notify warn (or higher) recipient due to missing profile: {}", recipient);
                }
                return;
            }
            IUserProfile profile = optProfile.get();
            String mailAddress = profile.getOption(MAIL_ADDRESS_PROFILE_OPTION, null);

            if (mailAddress == null) {
                if (notification.getUrgency() >= Urgency.WARN.getLevel()) {
                    logger.warn("Cannot notify warn (or higher) recipient due to missing profile option: {}/{}", recipient, MAIL_ADDRESS_PROFILE_OPTION);
                }
                return;
            }
            mailReceivers.add(mailAddress);
        });

        mailReceivers.forEach(receiver -> {
            IMail mail = IMail.builder()
                    .addSubjectText("[Jeak-Notification] " + notification.getSummary())
                    .addText("The following notification has been sent to you:\n" + notification.getLongText())
                    .to(receiver)
                    .build();

            transportUnit.dispatch(mail);
        });
    }


}
