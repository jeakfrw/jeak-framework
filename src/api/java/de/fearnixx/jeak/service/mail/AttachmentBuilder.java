package de.fearnixx.jeak.service.mail;

import javax.activation.DataSource;
import java.io.File;
import java.nio.file.Path;

public class AttachmentBuilder {

    public IAttachment fromFile(String name, File attachment) {
        return new AttachmentImpl(name, attachment, null, null);
    }

    public IAttachment fromPath(String name, Path attachment) {
        return new AttachmentImpl(name, null, attachment, null);
    }

    public IAttachment fromNative(String name, DataSource source) {
        return new AttachmentImpl(name, null, null, source);
    }

    private static class AttachmentImpl implements IAttachment {

        private final String attachmentName;
        private final File fileSource;
        private final Path pathSource;
        private final DataSource dataSource;

        public AttachmentImpl(String attachmentName, File fileSource, Path pathSource, DataSource dataSource) {
            this.attachmentName = attachmentName;
            this.fileSource = fileSource;
            this.pathSource = pathSource;
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
