package de.fearnixx.jeak.service.mail;

import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;

public interface IAttachment {

    static AttachmentBuilder builder() {
        return new AttachmentBuilder();
    }

    boolean isFileSource();

    File getFileSource();

    boolean isPathSource();

    Path getPathSource();

    boolean isReaderSource();

    Reader getReaderSource();

    boolean isURISource();

    URI getURISource();
}
