package de.fearnixx.jeak.voice.sound;

import java.io.InputStream;

public interface IMp3AudioPlayer {

    /**
     * Starts the audio player. Has to be called to initialize the audio player
     */
    void start();

    /**
     * Stops the current file
     */
    void stop();

    /**
     * Starts playing the current selected audio file
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
}
