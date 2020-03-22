package de.fearnixx.jeak.teamspeak.voice.sound;

import com.github.manevolent.ffmpeg4j.*;
import com.github.manevolent.ffmpeg4j.filter.audio.FFmpegAudioResampleFilter;
import com.github.manevolent.ffmpeg4j.source.FFmpegAudioSourceSubstream;
import com.github.manevolent.ffmpeg4j.stream.source.FFmpegSourceStream;
import de.fearnixx.jeak.teamspeak.voice.sound.opus.OpusParameters;
import de.fearnixx.jeak.voice.sound.AudioType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handling the playing of audio is extracted from an example by manevolent.
 * <p>
 * URL: https://github.com/Manevolent/ts3j/blob/55b10d6ba5d116769d4ef256d763beed9993fd56/examples/audio/src/main/java/com/github/manevolent/ts3j/examples/audio/MusicPlayer.java
 */
public class Mp3AudioPlayer extends AudioPlayer {

    private static final String MP3_EXTENSION = "mp3";
    private double volume = 0.5D;
    private static final long DELAY = 150 * 1_000_000L; // 50ms interval

    static {
        try {
            FFmpeg.register();
        } catch (FFmpegException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean paused = true;
    private boolean stopped = false;

    private FFmpegAudioSourceSubstream audioSourceSubstream;
    private LinkedBlockingQueue<AudioFrame> frameQueue;
    private Runnable endOfStreamCallback;

    public Mp3AudioPlayer() {
        super(AUDIO_FORMAT,
                (int) AUDIO_FORMAT.getSampleRate() * AUDIO_FORMAT.getChannels() * 4 /*4=32bit float*/,
                new OpusParameters(
                        20,
                        96000, // 96kbps
                        10, // max complexity
                        0, // 0 expected packet loss
                        false, // no VBR
                        false, // no FEC
                        true // OPUS MUSIC - channel doesn't have to be Opus Music ;)
                )
        );
    }

    public Mp3AudioPlayer(InputStream inputStream) {
        this();

        if (inputStream != null) {
            audioSourceSubstream = createAudioInputStream(inputStream);
        }
    }

    @Override
    public void setEndOfStreamCallback(Runnable endOfStreamCallback) {
        this.endOfStreamCallback = endOfStreamCallback;
    }

    @Override
    public void stop() {
        if (!paused) {
            pause();
        }

        try {
            audioSourceSubstream.close();
        } catch (Exception e) {
            throw new RuntimeException("Audio stream could not be closed!");
        }

        stopped = true;

        if (endOfStreamCallback != null) {
            endOfStreamCallback.run();
        }
    }

    @Override
    public void play() {
        new Thread(this::handlePlay).start();
    }

    private void handlePlay() {
        if (frameQueue != null) {
            frameQueue.clear();
        }

        startWrite();

        int bufferSize = AUDIO_FORMAT.getChannels() * (int) AUDIO_FORMAT.getSampleRate(); // Just to keep it orderly
        try (
                FFmpegAudioResampleFilter resampleFilter = new FFmpegAudioResampleFilter(
                        audioSourceSubstream.getFormat(),
                        new AudioFormat(
                                (int) AUDIO_FORMAT.getSampleRate(),
                                AUDIO_FORMAT.getChannels(),
                                FFmpeg.guessFFMpegChannelLayout(AUDIO_FORMAT.getChannels())
                        ),
                        bufferSize
                )) {

            this.frameQueue = new LinkedBlockingQueue<>();
            AudioFrame currentFrame = null;
            int frameOffset = 0; // offset within current frame

            long wake = System.nanoTime();
            long sleep;

            paused = false;

            while (true) {

                int available = availableInput();

                if (available > 0) {
                    if (currentFrame == null || frameOffset >= currentFrame.getLength()) {
                        if (frameQueue.peek() == null) {
                            try {
                                AudioFrame frame = audioSourceSubstream.next();
                                for (int i = 0; i < frame.getLength(); i++)
                                    frame.getSamples()[i] *= volume;

                                Collection<AudioFrame> frameList = resampleFilter.apply(frame);
                                frameQueue.addAll(frameList);
                            } catch (IOException ex) {
                                // flush currentFrame
                                break;
                            }
                        }

                        if (frameQueue.isEmpty()) continue;

                        currentFrame = frameQueue.remove();
                        frameOffset = 0;
                    }

                    int write = Math.min(currentFrame.getLength() - frameOffset, available);

                    write(
                            currentFrame.getSamples(),
                            frameOffset,
                            write
                    );

                    frameOffset += write;

                    continue;
                }

                wake += DELAY;
                sleep = (wake - System.nanoTime()) / 1_000_000;

                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (available < 0) {
                    stop();
                    return;
                }
            }

            try {
                drain();
                stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to stream audio!", e);
        }
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void pause() {
        if (frameQueue != null) {
            frameQueue.clear();
        }
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public synchronized byte[] provide() {
        if (!paused) {
            return super.provide();
        } else {
            return new byte[0];
        }
    }

    private FFmpegAudioSourceSubstream createAudioInputStream(InputStream inputStream) {
        try {
            FFmpegInput input = new FFmpegInput(inputStream);

            //There might be an awkward reason - but this stream cant be used with try-with-resource

            FFmpegSourceStream stream = input.open(FFmpeg.getInputFormatByName(getFilenameExtension()));
            return (FFmpegAudioSourceSubstream) stream.registerStreams()
                    .stream()
                    .filter(x -> x.getMediaType() == MediaType.AUDIO)
                    .findFirst().orElseThrow(
                            () -> new IllegalArgumentException("The supplied input stream did not contain an audio stream!")
                    );
        } catch (FFmpegException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void setAudioStream(InputStream inputStream) {

        if (audioSourceSubstream != null && !stopped) {
            try {
                audioSourceSubstream.close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        audioSourceSubstream = createAudioInputStream(inputStream);
    }

    @Override
    public boolean isPlaying() {
        return !paused;
    }

    @Override
    public AudioType getAudioType() {
        return AudioType.MP3;
    }

    @Override
    String getFilenameExtension() {
        return MP3_EXTENSION;
    }
}
