package de.fearnixx.jeak.service.mail;

import javax.activation.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Builder for e-mail attachments.
 */
public class AttachmentBuilder {

    AttachmentBuilder() {
    }

    /**
     * Creates a new attachment to be read from a {@link File}.
     * @throws IllegalArgumentException for non-readable or nonexistent files,  and directories
     */
    public IAttachment fromFile(String name, File attachment) {
        return new AttachmentImpl(name, attachment, null, null);
    }

    /**
     * Creates a new attachment to be read from a {@link Path}.
     * @throws IllegalArgumentException for non-readable or nonexistent files,  and directories
     */
    public IAttachment fromPath(String name, Path attachment) {
        return new AttachmentImpl(name, null, attachment, null);
    }

    /**
     * Creates a new attachment to be read from a native {@link DataSource}.
     */
    public IAttachment fromNative(String name, DataSource source) {
        return new AttachmentImpl(name, null, null, source);
    }

    private static class AttachmentImpl implements IAttachment {

        private final String attachmentName;
        private final File fileSource;
        private final Path pathSource;
        private final DataSource dataSource;

        public AttachmentImpl(String attachmentName, File fileSource, Path pathSource, DataSource dataSource) {
            Objects.requireNonNull(attachmentName, "File name may not be NULL!");
            this.attachmentName = attachmentName;

            if (fileSource != null && (fileSource.isDirectory() || !fileSource.isFile() || !fileSource.canRead())) {
                throw new IllegalArgumentException("File attachments must be readable files!");
            } else {
                this.fileSource = fileSource;
            }

            if (pathSource != null && (Files.isDirectory(pathSource) || !Files.isReadable(pathSource))) {
                throw new IllegalArgumentException("File attachments must be readable files!");
            } else {
                this.pathSource = pathSource;
            }

            this.dataSource = dataSource;
        }

        @Override
        public String getName() {
            return attachmentName;
        }

        @Override
        public boolean isFileSource() {
            return fileSource != null;
        }

        @Override
        public File getFileSource() {
            return fileSource;
        }

        @Override
        public boolean isPathSource() {
            return pathSource != null;
        }

        @Override
        public Path getPathSource() {
            return pathSource;
        }

        @Override
        public boolean isNativeSource() {
            return dataSource != null;
        }

        @Override
        public DataSource getNativeSource() {
            return dataSource;
        }
    }
}
