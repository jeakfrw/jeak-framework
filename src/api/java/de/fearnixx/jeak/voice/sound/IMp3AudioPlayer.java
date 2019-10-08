package de.fearnixx.jeak.voice.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Represents an audio-player that can play mp3-files
 */
public interface IMp3AudioPlayer {

    /**
     * Stops playing the current file
     */
    void stop();

    /**
     * Starts playing the current audio file
     */
    void play();

    /**
     * Pauses playing the current audio file
     */
    void pause();

    /**
     * Resumes the current audio file
     */
    void resume();

    /**
     * Sets the input-stream for the audio player
     *
     * @param inputStream the audio
     */
    void setAudioFile(InputStream inputStream);

    /**
     * Sets the audio-file for the audio player
     *
     * @param parentDir parent-dir of the audio-file
     * @param filename  name of the audio file
     */
    void setAudioFile(File parentDir, String filename) throws FileNotFoundException;

    /**
     * @return whether the audio-player is currently playing
     */
    boolean isPlaying();
}
