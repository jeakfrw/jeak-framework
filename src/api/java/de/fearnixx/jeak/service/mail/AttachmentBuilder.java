package de.fearnixx.jeak.service.mail;

import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;

public class AttachmentBuilder {

    public IAttachment fromFile(File attachment) {
        return new AttachmentImpl(attachment, null, null, null);
    }

    public IAttachment fromPath(Path attachment) {
        return new AttachmentImpl(null, attachment, null, null);
    }

    public IAttachment fromURI(URI attachment) {
        return new AttachmentImpl(null, null, attachment, null);
    }

    public IAttachment fromReader(Reader attachment) {
        return new AttachmentImpl(null, null, null, attachment);
    }

    private static class AttachmentImpl implements IAttachment {

        private final File fileSource;
        private final Path pathSource;
        private final URI uriSource;
        private final Reader readerSource;

        public AttachmentImpl(File fileSource, Path pathSource, URI uriSource, Reader readerSource) {
            this.fileSource = fileSource;
            this.pathSource = pathSource;
            this.uriSource = uriSource;
            this.readerSource = readerSource;
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
        public boolean isReaderSource() {
            return readerSource != null;
        }

        @Override
        public Reader getReaderSource() {
            return readerSource;
        }

        @Override
        public boolean isURISource() {
            return uriSource != null;
        }

        @Override
        public URI getURISource() {
            return uriSource;
        }
    }
}
