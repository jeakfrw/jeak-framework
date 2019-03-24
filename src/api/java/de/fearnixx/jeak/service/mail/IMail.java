package de.fearnixx.jeak.service.mail;

import java.util.List;
import java.util.Map;

/**
 * Interface for e-mail messages to be sent by a {@link ITransportUnit} to a number of recipients.
 * @implNote Internally, JavaMail (from Jakarta) is used. This interface shall simplify message construction.
 */
public interface IMail {

    /**
     * Initialize builder-pattern.
     */
    static MailBuilder builder() {
        return new MailBuilder();
    }

    /**
     * Any number of custom headers to add to the message.
     * @implNote custom headers are applied AFTER message construction so they are able to override automatic headers.
     */
    Map<String, String> getHeaders();

    /**
     * Recipients for the "TO" parameter.
     * @implNote must be valid for InternetAddresses used in JavaMail.
     */
    List<String> getRecipientsTO();

    /**
     * Recipients for the "CC" parameter.
     * @implNote must be valid for InternetAddresses used in JavaMail.
     */
    List<String> getRecipientsCC();

    /**
     * Recipients for the "BCC" parameter.
     * @implNote must be valid for InternetAddresses used in JavaMail.
     */
    List<String> getRecipientsBCC();

    /**
     * Message subject.
     */
    String getSubject();

    /**
     * Message body - the actual text.
     * @implNote Mime type "text/html" is applied to this.
     */
    String getBody();

    /**
     * List of attachments to be added to this message.
     * @see IAttachment
     */
    List<IAttachment> getAttachments();
}
