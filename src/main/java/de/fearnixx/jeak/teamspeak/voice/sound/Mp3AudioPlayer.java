package de.fearnixx.jeak.teamspeak.voice.sound;

import com.github.manevolent.ffmpeg4j.*;
import com.github.manevolent.ffmpeg4j.filter.audio.FFmpegAudioResampleFilter;
import com.github.manevolent.ffmpeg4j.source.FFmpegAudioSourceSubstream;
import com.github.manevolent.ffmpeg4j.stream.source.FFmpegSourceStream;
import de.fearnixx.jeak.teamspeak.voice.sound.opus.OpusParameters;
import de.fearnixx.jeak.voice.sound.AudioType;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class Mp3AudioPlayer extends AudioPlayer {

    private static final String MP3_EXTENSION = "mp3";

    static {
        try {
            FFmpeg.register();
        } catch (FFmpegException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean paused = true;
    private FFmpegAudioSourceSubstream audioSourceSubstream;
    private LinkedBlockingQueue<AudioFrame> frameQueue;

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

        startWrite();
    }

    public Mp3AudioPlayer(InputStream fileInputStream) {
        this();

        if (fileInputStream != null) {
            audioSourceSubstream = createAudioInputStream(fileInputStream);
        }
    }

    @Override
    public void stop() {
        if (!paused) {
            pause();
        }

        stopWrite();
    }

    @Override
    public void play() {
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
            long delay = 150 * 1_000_000L; // 50ms interval
            long sleep;
            double volume = 0.5D;

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

                wake += delay;
                sleep = (wake - System.nanoTime()) / 1_000_000;

                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (available < 0) {
                    paused = true;
                    break;
                }
            }

            try {
                drain();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to stream audio!", e);
        }
    }

    public void pause() {
        frameQueue.clear();
        paused = true;
    }

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
                    .findFirst().orElse(null);

        } catch (FFmpegException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void setAudioFile(InputStream inputStream) {
        audioSourceSubstream = createAudioInputStream(inputStream);
    }

    @Override
    public void setAudioFile(File parentDir, String filename) throws FileNotFoundException {
        String filepath = filename;
        if (!filepath.toLowerCase().endsWith(getFilenameExtension())) {
            filepath += "." + getFilenameExtension();
        }

        setAudioFile(new FileInputStream(new File(parentDir, filepath)));
    }

    @Override
    public boolean isPlaying() {
        return !paused;
    }

    @Override
    public AudioType getAudioType() {
        return AudioType.MP3;
    }

    private String getFilenameExtension() {
        return MP3_EXTENSION;
    }
}
