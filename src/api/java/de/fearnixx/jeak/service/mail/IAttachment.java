package de.fearnixx.jeak.service.mail;

import javax.activation.DataSource;
import java.io.File;
import java.nio.file.Path;

public interface IAttachment {

    static AttachmentBuilder builder() {
        return new AttachmentBuilder();
    }

    String getName();

    boolean isFileSource();

    File getFileSource();

    boolean isPathSource();

    Path getPathSource();

    boolean isNativeSource();

    DataSource getNativeSource();
}
