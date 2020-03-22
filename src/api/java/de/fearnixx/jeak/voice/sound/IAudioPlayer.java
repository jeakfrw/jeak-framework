package de.fearnixx.jeak.voice.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Represents an audio-player that is able to play audio files
 */
public interface IAudioPlayer {

    /**
     * Stops playing the current audio, by stopping to read from the underlying stream.
     */
    void stop();

    /**
     * Starts playing the current audio file
     */
    void play();

    /**
     * Pauses playing the current audio by streaming empty-packets instead of the underlying stream.
     */
    void pause();

    /**
     * Resumes the current audio file
     */
    void resume();

    /**
     * Sets the input-stream for the audio player
     *
     * @param inputStream the input stream of the audio
     */
    void setAudioStream(InputStream inputStream);

    /**
     * Sets the input stream for the audio player based on the given audio-file
     *
     * @param audioFile the audio-file
     */
    void setAudioFile(File audioFile) throws FileNotFoundException;

    /**
     * Sets the audio-file for the audio player based on the filename and a parent directory
     *
     * @param parentDir parent-dir of the audio-file
     * @param filename  name of the audio file
     */
    void setAudioFile(File parentDir, String filename) throws FileNotFoundException;


    /**
     * Sets the input stream for the audio player based on the given path to the audio-file
     *
     * @param audioFilePath path to the audio-file
     */
    void setAudioFile(Path audioFilePath) throws FileNotFoundException;

    /**
     * @return whether the audio-player is currently playing
     */
    boolean isPlaying();

    /**
     * Sets the volume of the audio player
     *
     * @param volume The permitted values reach from 0 to 1 (inclusive)
     */
    void setVolume(double volume);

    /**
     * @return the type which the audio player is designated for
     */
    AudioType getAudioType();
}