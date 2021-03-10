package de.fearnixx.jeak.service.mail;

import de.mlessmann.confort.api.IConfigNode;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public SmtpTransportUnit(String unitName, ExecutorService executorService) {
        this.unitName = unitName;
        this.executorService = executorService;
    }

    public void load(IConfigNode configuration) throws MessagingException {
        jMailProperties = new Properties();

        configuration.getNode("username").optString()
                .or(() -> configuration.getNode("user").optString())
                .ifPresent(user -> {
                    jMailProperties.setProperty("mail.smtp.auth", "true");
                    jMailProperties.setProperty("mail.smtp.user", user);
                });

        String authPassword = configuration.getNode("pass")
                .optString()
                .or(() -> configuration.getNode("password").optString())
                .orElse("webmaster");
        jMailProperties.setProperty("mail.smtp.pass", authPassword);
        jMailProperties.setProperty("mail.smtp.starttls.enable", configuration.getNode("starttls").optString("true"));
        jMailProperties.setProperty("mail.smtp.host", configuration.getNode("host").optString("localhost"));
        jMailProperties.setProperty("mail.smtp.port", configuration.getNode("port").optString("25"));
        jMailProperties.setProperty("mail.smtp.from", configuration.getNode("from").optString("mail@localhost"));

        configuration.getNode("properties")
                .optMap()
                .ifPresent(map -> map.forEach((k, v) -> jMailProperties.setProperty(k, v.asString())));

        if ("true".equals(jMailProperties.getProperty("mail.smtp.auth"))) {
            jMailSession = Session.getInstance(jMailProperties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            jMailProperties.getProperty("mail.smtp.user"),
                            jMailProperties.getProperty("mail.smtp.pass")
                    );
                }
            });
        } else {
            jMailSession = Session.getInstance(jMailProperties);
        }
        jMailSmtp = jMailSession.getTransport("smtp");
    }

    public void dispatch(IMail message) {
        buildMime(message).ifPresent(this::dispatch);
    }

    public void dispatch(MimeMessage message) {
        executorService.execute(() -> {
            Address[] recips = null;
            try {
                recips = message.getAllRecipients();
                jMailSmtp.connect();
                jMailSmtp.sendMessage(message, recips);
                jMailSmtp.close();
                logger.debug("[{}] Successfully sent message.", unitName);

            } catch (MessagingException e) {
                logger.warn("[{}] Failed to dispatch message! Recipients: {}", unitName, recips, e);

            } finally {
                if (jMailSmtp.isConnected()) {
                    try {
                        jMailSmtp.close();
                    } catch (MessagingException e) {
                        logger.warn("[{}] Failed to close SMTP connection.", unitName, e);
                    }
                }
            }
        });
    }

    protected Optional<MimeMessage> buildMime(IMail message) {
        try {
            MimeMessage mimeMessage = new MimeMessage(jMailSession);
            MimeMultipart mimeMultipart = new MimeMultipart();

            logger.debug("[{}] Adding recipients.", unitName);
            message.getRecipientsTO().forEach(rec ->
                    addRecipient(mimeMessage, rec, MimeMessage.RecipientType.TO));
            logger.debug("To: {}", message.getRecipientsTO());
            message.getRecipientsCC().forEach(rec ->
                    addRecipient(mimeMessage, rec, MimeMessage.RecipientType.CC));
            logger.debug("CC: {}", message.getRecipientsCC());
            message.getRecipientsBCC().forEach(rec ->
                    addRecipient(mimeMessage, rec, MimeMessage.RecipientType.BCC));
            logger.debug("BCC: {}", message.getRecipientsBCC());
            mimeMessage.setSubject(message.getSubject(), CHARSET.toString());

            logger.debug("[{}] Adding attachments.", unitName);
            if (!message.getAttachments().isEmpty()) {
                BodyPart textBody = new MimeBodyPart();
                textBody.setContent(message.getBody(), "text/html");
                mimeMultipart.addBodyPart(textBody);

                message.getAttachments()
                        .forEach(a -> {
                            try {
                                DataSource attachmentSource;
                                if (a.isFileSource()) {
                                    attachmentSource = new FileDataSource(a.getFileSource().getAbsoluteFile());
                                } else if (a.isPathSource()) {
                                    attachmentSource = new FileDataSource(a.getPathSource().toFile().getAbsoluteFile());
                                } else if (a.isNativeSource()) {
                                    attachmentSource = ((DataSource) a.getNativeSource());
                                } else {
                                    logger.warn("Failed to attach unknown attachment type! {}", a);
                                    return;
                                }

                                MimeBodyPart bodyPart = new MimeBodyPart();
                                bodyPart.setFileName(a.getName());
                                bodyPart.setDataHandler(new DataHandler(attachmentSource));

                                mimeMultipart.addBodyPart(bodyPart);
                            } catch (MessagingException e) {
                                logger.warn("[{}] Failed to add/build attachment source.", unitName, e);
                            }
                        });
                mimeMessage.setContent(mimeMultipart);

            } else {
                mimeMessage.setText(message.getBody(), CHARSET.toString());
            }


            logger.debug("[{}] Adding headers.", unitName);
            message.getHeaders().forEach((k, v) -> {
                try {
                    mimeMessage.addHeader(k, v);
                } catch (MessagingException e) {
                    logger.warn("[{}] Failed to add header to MIME message: {}: {}", unitName, k, v, e);
                }
            });

            return Optional.of(mimeMessage);
        } catch (MessagingException e) {
            logger.warn("[{}] Failed to construct mail message for unit.", unitName, e);
            return Optional.empty();
        }
    }

    protected void addRecipient(MimeMessage mimeMessage, String rec, Message.RecipientType type) {
        try {
            Address addr = new InternetAddress(rec);
            mimeMessage.addRecipient(type, addr);
        } catch (MessagingException e) {
            logger.warn("[{}] Failed to add recipient to mail: \"{}\"", unitName, e);
        }
    }
}
