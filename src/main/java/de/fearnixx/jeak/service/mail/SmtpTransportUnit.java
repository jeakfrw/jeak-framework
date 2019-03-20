package de.fearnixx.jeak.service.mail;

import de.mlessmann.confort.api.IConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class SmtpTransportUnit implements ITransportUnit {

    private static final Logger logger = LoggerFactory.getLogger(SmtpTransportUnit.class);
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final ExecutorService executorService;
    private final String unitName;
    private Properties jMailProperties;
    private Transport jMailSmtp;
    private Session jMailSession;
    private MimeMessage defaultMIME;

    public SmtpTransportUnit(String unitName, ExecutorService executorService) {
        this.unitName = unitName;
        this.executorService = executorService;
    }

    public void load(IConfigNode configuration) throws MessagingException {

        jMailSession = Session.getInstance(jMailProperties);
        jMailSmtp = jMailSession.getTransport("smtp");
        defaultMIME = new MimeMessage(jMailSession);
    }

    public MimeMessage getDefaultMIME() {
        return defaultMIME;
    }

    public void dispatch(IMail message) {
        buildMime(message).ifPresent(this::dispatch);
    }

    public void dispatch(MimeMessage message) {
        executorService.execute(() -> {
            try {
                jMailSmtp.sendMessage(message, message.getAllRecipients());
            } catch (MessagingException e) {
                logger.warn("[{}] Failed to dispatch message!", unitName, e);
            }
        });
    }

    private Optional<MimeMessage> buildMime(IMail message) {
        try {
            MimeMessage mimeMessage = new MimeMessage(getDefaultMIME());

            message.getRecipientsTO().forEach(rec ->
                    addRecipient(mimeMessage, rec, MimeMessage.RecipientType.TO));
            message.getRecipientsCC().forEach(rec ->
                    addRecipient(mimeMessage, rec, MimeMessage.RecipientType.CC));
            message.getRecipientsBCC().forEach(rec ->
                    addRecipient(mimeMessage, rec, MimeMessage.RecipientType.BCC));

            message.getHeaders().forEach((k, v) -> {
                try {
                    mimeMessage.addHeader(k, v);
                } catch (MessagingException e) {
                    logger.warn("[{}] Failed to add header to MIME message: {}: {}", unitName, k, v, e);
                }
            });

            mimeMessage.setSubject(message.getSubject(), CHARSET.toString());
            mimeMessage.setText(message.getBody(), CHARSET.toString());

            return Optional.of(mimeMessage);
        } catch (MessagingException e) {
            logger.warn("[{}] Failed to construct mail message for unit.", unitName, e);
            return Optional.empty();
        }
    }

    private void addRecipient(MimeMessage mimeMessage, String rec, Message.RecipientType type) {
        try {
            Address addr = new InternetAddress(rec);
            mimeMessage.addRecipient(type, addr);
        } catch (MessagingException e) {
            logger.warn("[{}] Failed to add recipient to mail: \"{}\"", unitName, e);
        }
    }
}
