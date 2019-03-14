package de.fearnixx.jeak.service.mail;

import java.util.List;
import java.util.Map;

public interface IMail {

    static MailBuilder builder() {
        return new MailBuilder();
    }

    Map<String, String> getHeaders();

    List<String> getRecipientsTO();

    List<String> getRecipientsCC();

    List<String> getRecipientsBCC();

    String getBody();

    List<IAttachment> getAttachments();
}
