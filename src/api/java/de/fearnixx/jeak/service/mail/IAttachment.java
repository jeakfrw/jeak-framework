package de.fearnixx.jeak.service.mail;

import javax.activation.DataSource;
import java.io.File;
import java.nio.file.Path;

/**
 * Interface for adding files to a {@link IMail}.
 */
public interface IAttachment {

    static AttachmentBuilder builder() {
        return new AttachmentBuilder();
    }

    /**
     * Name used to attach the file.
     */
    String getName();

    /**
     * Whether or not the {@link File} representation should be attached.
     */
    boolean isFileSource();

    /**
     * The {@link File} representation.
     */
    File getFileSource();

    /**
     * Whether or not the {@link Path} representation should be attached.
     * @implNote Internally, {@link Path#toFile()} is used.
     */
    boolean isPathSource();

    /**
     * The {@link Path} representation.
     */
    Path getPathSource();

    /**
     * Plugins can also directly use the {@link javax.activation} classes for attachments.
     * Whether or not this representation should be used.
     */
    boolean isNativeSource();

    /**
     * The {@link DataSource} representation of the attachment.
     * @implNote Allows for non-file representations to be used. Such as {@link javax.activation.URLDataSource}.
     */
    DataSource getNativeSource();
}
