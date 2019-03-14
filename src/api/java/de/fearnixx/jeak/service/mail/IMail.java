package de.fearnixx.jeak.service.mail;

public interface IMail {

    static MailBuilder builder() {
        return new MailBuilder();
    }
}
