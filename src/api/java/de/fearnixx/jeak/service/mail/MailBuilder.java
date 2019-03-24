package de.fearnixx.jeak.service.mail;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builder for e-mails.
 */
public class MailBuilder {

    private final Map<String, String> headers = new LinkedHashMap<>();
    private final List<String> receiversTO = new LinkedList<>();
    private final List<String> receiversCC = new LinkedList<>();
    private final List<String> receiversBCC = new LinkedList<>();
    private final StringBuilder subject = new StringBuilder();
    private final StringBuilder body = new StringBuilder();
    private final List<IAttachment> attachments = new LinkedList<>();

    MailBuilder() {
    }

    /**
     * Add a "TO" recipient.
     * @implNote Must be a valid InternetAddress as used in {@code javax.mail}
     */
    public MailBuilder to(String recipient) {
        if (!receiversTO.contains(recipient)) {
            receiversTO.add(recipient);
        }
        return this;
    }

    /**
     * Add a "CC" recipient.
     * @implNote Must be a valid InternetAddress as used in {@code javax.mail}
     */
    public MailBuilder toCC(String recipient) {
        if (!receiversCC.contains(recipient)) {
            receiversCC.add(recipient);
        }
        return this;
    }

    /**
     * Add a "BCC" recipient.
     * @implNote Must be a valid InternetAddress as used in {@code javax.mail}
     */
    public MailBuilder toBCC(String recipient) {
        if (!receiversBCC.contains(recipient)) {
            receiversBCC.add(recipient);
        }
        return this;
    }

    /**
     * Adds text to the subject.
     * Alternatively, use the direct access via {@link #subject()}.
     */
    public MailBuilder addSubjectText(String text) {
        subject.append(text);
        return this;
    }

    /**
     * Allows direct access to the subject builder.
     */
    public StringBuilder subject() {
        return subject;
    }

    /**
     * Adds text to the (HTML) body.
     * Alternatively, use the direct access via. {@link #body()}.
     */
    public MailBuilder addText(String text) {
        body.append(text);
        return this;
    }

    /**
     * Allows direct access to the body builder.
     */
    public StringBuilder body() {
        return body;
    }

    /**
     * Add an {@link IAttachment} to the e-mail.
     */
    public MailBuilder attach(IAttachment attachment) {
        if (!attachments.contains(attachment)) {
            attachments.add(attachment);
        }
        return this;
    }

    /**
     * Set custom header for the e-mail.
     */
    public MailBuilder header(String headerName, String headerValue) {
        if (headerValue == null) {
            headers.remove(headerName);
        } else {
            headers.put(headerName, headerValue);
        }
        return this;
    }

    public IMail build() {
        final LinkedHashMap<String, String> fHeaders = new LinkedHashMap<>(headers);
        final LinkedList<String> fRecipientsTO = new LinkedList<>(receiversTO);
        final LinkedList<String> fRecipientsCC = new LinkedList<>(receiversCC);
        final LinkedList<String> fRecipientsBCC = new LinkedList<>(receiversBCC);
        final String fSubject = subject.toString();
        final String fBody = body.toString();
        final LinkedList<IAttachment> fAttachments = new LinkedList<>(attachments);

        return new IMail() {
            @Override
            public Map<String, String> getHeaders() {
                return fHeaders;
            }

            @Override
            public List<String> getRecipientsTO() {
                return fRecipientsTO;
            }

            @Override
            public List<String> getRecipientsCC() {
                return fRecipientsCC;
            }

            @Override
            public List<String> getRecipientsBCC() {
                return fRecipientsBCC;
            }

            @Override
            public String getSubject() {
                return fSubject;
            }

            @Override
            public String getBody() {
                return fBody;
            }

            @Override
            public List<IAttachment> getAttachments() {
                return fAttachments;
            }
        };
    }
}
