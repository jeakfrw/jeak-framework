package de.fearnixx.jeak.teamspeak.query.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * An implementation of {@link ByteChannel} that combines {@link ReadableByteChannel}s and {@link WritableByteChannel}s
 * created by {@link Channels#newChannel(InputStream)} and {@link Channels#newChannel(OutputStream)} into one.
 */
public class StreamBasedChannel implements ByteChannel {

    private final ReadableByteChannel readable;
    private final WritableByteChannel writeable;
    private final OutputStream writeTarget;
    private final boolean autoFlush;

    public static StreamBasedChannel create(InputStream in, OutputStream out) {
        return create(in, out, true);
    }

    public static StreamBasedChannel create(InputStream in, OutputStream out, boolean autoFlush) {
        return new StreamBasedChannel(
                Channels.newChannel(in),
                Channels.newChannel(out),
                out,
                autoFlush
        );
    }

    protected StreamBasedChannel(ReadableByteChannel readable, WritableByteChannel writeable, OutputStream out, boolean autoFlush) {
        this.readable = readable;
        this.writeable = writeable;
        this.writeTarget = out;
        this.autoFlush = autoFlush;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return readable.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        final var count = writeable.write(src);
        if (autoFlush) {
            writeTarget.flush();
        }
        return count;
    }

    @Override
    public boolean isOpen() {
        return writeable.isOpen() && readable.isOpen();
    }

    @Override
    public void close() throws IOException {
        IOException rt = null;
        try {
            if (writeable.isOpen()) {
                writeable.close();
            }
        } catch (IOException e) {
            rt = e;
        }
        try {
            if (readable.isOpen()) {
                readable.close();
            }
        } catch (IOException e) {
            rt = rt != null ? rt : e;
        }
        if (rt != null) {
            throw rt;
        }
    }
}
