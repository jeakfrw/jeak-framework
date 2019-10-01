package de.fearnixx.jeak.teamspeak.voice.sound;

import com.github.manevolent.ts3j.audio.Microphone;
import com.github.manevolent.ts3j.enums.CodecType;
import de.fearnixx.jeak.teamspeak.voice.sound.opus.OpusEncoder;
import de.fearnixx.jeak.teamspeak.voice.sound.opus.OpusParameters;
import net.tomp2p.opuswrapper.Opus;

import javax.sound.sampled.AudioFormat;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeamspeakFastMixerSink implements Microphone {
    static final AudioFormat AUDIO_FORMAT =
            new AudioFormat(48000, 32, 2, true, false);

    private final Object stateLock = new Object();
    private final Object drainLock = new Object();

    // Audio format
    private final AudioFormat audioFormat; // Audio output format (mostly just ensured to match certain parameters)

    // OPUS variables
    private final OpusParameters opusParameters; // Opus parameters set by configurations
    private volatile OpusEncoder encoder = null; // Opus encoder instance
    private final int opusFrameSize; // Opus frame size (PER CHANNEL!) (also the ASIO chunk size)
    private long opusPacketsEncoded = 0;
    private long opusPacketsSent = 0;
    private long opusTime = 0;
    private long networkTime = 0;
    private long opusPosition = 0;
    private long opusBytePosition = 0;

    // Statistics variables
    private long underflowed = 0; // Underflowed samples (ASIO has to return 0 samples to the encoder)
    private long overflowed = 0; // Overflowed samples (write returns 0)
    private long position = 0; // Position in samples

    // I/O variables
    private final Queue<OpusPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final int bufferSize; // Buffer size, in samples
    private volatile int availableInput;
    private volatile int samplePosition; // Position, in samples, of the ASIO buffer (available samples)
    private final float[] sampleBuffer; // ASIO buffer

    // Mixer sink state variables
    private volatile boolean running = false;
    private volatile boolean opening = false;

    TeamspeakFastMixerSink(AudioFormat audioFormat,
                           int bufferSizeInBytes,
                           OpusParameters opusParameters) {

        this.audioFormat = audioFormat;

        if (audioFormat.getSampleRate() != 48000)
            throw new IllegalArgumentException("Invalid audio sample rate: " +
                    audioFormat.getSampleRate() + " != 48000: OPUS requires 48KHz audio");
        else if (audioFormat.getChannels() != 2)
            throw new IllegalArgumentException("Invalid audio channel count: " +
                    audioFormat.getChannels() + " != 2: OPUS requires stereo audio");

        this.bufferSize = bufferSizeInBytes / (audioFormat.getSampleSizeInBits() / 8);
        if (bufferSize <= 0 || bufferSize % audioFormat.getChannels() != 0)
            throw new IllegalArgumentException("invalid bufferSize: " + bufferSize);

        this.sampleBuffer = new float[bufferSize];
        this.availableInput = 0;

        this.opusParameters = opusParameters;
        this.opusFrameSize = (int) getAudioFormat().getSampleRate() / (1000 / opusParameters.getOpusFrameTime());
    }

    private AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void write(float[] buffer, int len) {
        write(buffer, 0, len);
    }

    void write(float[] buffer, int offs, int len) {
        synchronized (stateLock) {
            if (!running) return;

            if (len > availableInput()) { // This should never happen
                overflowed++; // Probably never will run
                throw new IllegalArgumentException(len + " > " + availableInput());
            }

            if (offs + len > buffer.length) {
                overflowed++;
                throw new IllegalArgumentException(offs + len + " > " + buffer.length);
            }

            if (len <= 0)
                throw new IllegalArgumentException(len + " <= 0");

            if (len % getChannels() != 0)
                throw new IllegalArgumentException("not a full frame");

            // Write to the buffer
            System.arraycopy(buffer, offs, sampleBuffer, samplePosition, len);
            samplePosition += len;
            availableInput -= len;

            // Read samples from the buffer and encode them into packets
            position += encode(false);
        }
    }

    /**
     * Encodes the buffer to packets for the packet queue
     *
     * @return Packets encoded
     */
    private synchronized int encode(boolean flush) {
        if (flush) Logger.getGlobal().log(Level.FINE, "Flushing TeamspeakFastMixerSink...");

        int written = 0;
        int frameSize = opusFrameSize * getChannels();
        byte[] encoded;
        long now;
        int copy;

        while (samplePosition >= (flush ? 1 : frameSize)) {
            copy = Math.min(samplePosition, frameSize);
            if (flush && copy < frameSize) {
                for (int i = copy; i < frameSize; i++)
                    sampleBuffer[i] = 0f;

                samplePosition = frameSize;
            }

            if (encoder == null) openOpusEncoder();

            now = System.nanoTime();
            encoded = encoder.encode(sampleBuffer, frameSize);
            opusTime += (System.nanoTime() - now);
            opusPacketsEncoded++;
            opusPosition += frameSize;

            System.arraycopy(sampleBuffer, frameSize, sampleBuffer, 0, samplePosition - frameSize);
            samplePosition -= frameSize;
            if (packetQueue.add(new OpusPacket(frameSize, encoded)))
                written += frameSize;
        }

        if (written > 0) opening = false;

        if (flush) Logger.getGlobal().log(Level.FINE, "Flushed TeamspeakFastMixerSink.");

        return written;
    }

    long getNanotime() {
        return opusTime;
    }

    long getFrameSize() {
        return opusFrameSize;
    }

    long getEncoderPosition() {
        return opusPosition;
    }

    long getNetworkPosition() {
        return opusBytePosition;
    }

    OpusParameters getEncoderParameters() {
        return opusParameters;
    }

    long getPacketsEncoded() {
        return opusPacketsEncoded;
    }

    long getPacketsSent() {
        return opusPacketsSent;
    }

    /**
     * Finds the count of available samples to be written to write() in the len param
     *
     * @return Available sample count
     */
    int availableInput() {
        return availableInput;
    }

    private boolean isRunning() {
        return running;
    }

    /**
     * Opens the Opus encoder
     */
    private void openOpusEncoder() {
        synchronized (stateLock) {
            if (encoder != null) return;
            Logger.getGlobal().log(Level.FINE, "Opening TeamspeakFastMixerSink encoder...");

            encoder = new OpusEncoder(
                    (int) getAudioFormat().getSampleRate(), // smp rate (always 48kHz)
                    opusFrameSize,
                    getChannels(),
                    audioFormat.isBigEndian()
                    //,MAXIMUM_OPUS_PACKET_SIZE // Prevent IP fragmentation
            );

            encoder.setEncoderValue(
                    Opus.OPUS_SET_SIGNAL_REQUEST,
                    opusParameters.isOpusMusic() ? Opus.OPUS_SIGNAL_MUSIC : Opus.OPUS_SIGNAL_VOICE
            );

            encoder.setEncoderValue(Opus.OPUS_SET_BITRATE_REQUEST, opusParameters.getOpusBitrate());
            encoder.setEncoderValue(Opus.OPUS_SET_COMPLEXITY_REQUEST, opusParameters.getOpusComplexity());
            encoder.setEncoderValue(Opus.OPUS_SET_PACKET_LOSS_PERC_REQUEST, opusParameters.getOpusPacketLoss());
            encoder.setEncoderValue(Opus.OPUS_SET_VBR_REQUEST, opusParameters.isOpusVbr() ? 1 : 0);
            encoder.setEncoderValue(Opus.OPUS_SET_INBAND_FEC_REQUEST, opusParameters.isOpusFec() ? 1 : 0);

            Logger.getGlobal().log(Level.FINE, "Opened TeamspeakFastMixerSink encoder.");
        }
    }

    private void closeOpusEncoder() {
        synchronized (stateLock) {
            if (encoder != null) {
                Logger.getGlobal().log(Level.FINE, "Closing TeamspeakFastMixerSink encoder...");
                encoder.close();
                encoder = null;
                Logger.getGlobal().log(Level.FINE, "Closed TeamspeakFastMixerSink encoder.");
            }
        }
    }

    void close() {
        if (isRunning()) stopWrite();

        closeOpusEncoder();
    }

    boolean startWrite() {
        synchronized (stateLock) {
            if (running) return false;

            Logger.getGlobal().log(Level.FINE, "Starting TeamspeakFastMixerSink...");

            // Flush buffers, clear outgoing packet queues.
            for (int i = 0; i < sampleBuffer.length; i++) sampleBuffer[i] = 0f;
            samplePosition = 0;
            availableInput = bufferSize;
            packetQueue.clear();

            // Open (or re-open) Opus encoder.
            openOpusEncoder();

            // Reset the encoder
            if (encoder != null) {
                Logger.getGlobal().log(Level.FINE, "Resetting TeamspeakFastMixerSink...");
                encoder.reset(); // Reset encoder output
                Logger.getGlobal().log(Level.FINE, "Reset TeamspeakFastMixerSink.");
            }

            // Mark as running.
            opening = true;
            running = true;

            Logger.getGlobal().log(Level.FINE, "Started TeamspeakFastMixerSink.");

            return true;
        }
    }

    void drain() throws InterruptedException {
        synchronized (drainLock) {
            while (running && packetQueue.peek() != null) {
                drainLock.wait();
            }
        }
    }

    /**
     * Closes mixer sink
     * <p>
     * Note that we don't close the Opus encoder - just in case there are more samples to write/flush out.
     */
    boolean stopWrite() {
        synchronized (stateLock) {
            if (!running) return false;

            Logger.getGlobal().log(Level.FINE, "Stopping TeamspeakFastMixerSink...");

            try {
                encode(true);
            } catch (RuntimeException e) {
                Logger.getGlobal().log(Level.WARNING, "Problem flushing audio buffer upon close", e);
            }

            running = false;
            Logger.getGlobal().log(Level.FINE, "Stopped TeamspeakFastMixerSink.");

            return true;
        }
    }

    int getBufferSize() {
        return bufferSize;
    }

    long getPosition() {
        return position;
    }

    long getUnderflows() {
        return underflowed;
    }

    long getOverflows() {
        return overflowed;
    }

    /**
     * Called by the encoder thread to determine if the sink is ready to send samples.
     *
     * @return true if the sink has samples to write, or if samples will become available.
     */
    @Override
    public boolean isReady() {
        return (running && !opening) || !packetQueue.isEmpty();
    }

    @Override
    public CodecType getCodec() {
        return CodecType.OPUS_MUSIC;
    }

    /**
     * Provides OPUS encoded audio (as a packet) to the TS3J UDP audio network thread.
     *
     * @return Encoded OPUS audio packet (zero-length packet if there is an underflow).
     */
    @Override
    public synchronized byte[] provide() {
        long start = System.nanoTime();

        try {
            if (packetQueue.peek() == null) {
                underflowed++;
                return new byte[0];
            }

            OpusPacket packet = packetQueue.remove();

            if (packet == null) {
                underflowed++;
                return new byte[0];
            }

            availableInput += packet.getSamples();
            opusBytePosition += packet.getBytes().length;
            opusPacketsSent++;

            return packet.getBytes();
        } catch (NoSuchElementException ex) {
            underflowed++;
            return new byte[0];
        } finally {
            long currentNetworkTime = System.nanoTime() - start;

            if (currentNetworkTime >= (opusParameters.getOpusFrameTime() * 1000000L)) {
                underflowed++;

                Logger.getGlobal().warning("[TeamspeakFastMixerSink] provide() took longer than the expected " +
                        opusParameters.getOpusFrameTime() + "ms: " + ((double) networkTime / 1_000_000D) + "ms");
            }

            this.networkTime += currentNetworkTime;

            if (packetQueue.peek() == null) {
                synchronized (drainLock) {
                    drainLock.notifyAll();
                }
            }
        }
    }

    int getChannels() {
        return getAudioFormat().getChannels();
    }

    private final class OpusPacket {
        private final int samples;
        private final byte[] bytes;

        OpusPacket(int samples, byte[] bytes) {
            this.samples = samples;
            this.bytes = bytes;
        }

        int getSamples() {
            return samples;
        }

        byte[] getBytes() {
            return bytes;
        }
    }
}