package de.fearnixx.jeak.teamspeak.voice.sound;

import de.fearnixx.jeak.teamspeak.voice.sound.opus.OpusParameters;
import de.fearnixx.jeak.voice.sound.IAudioPlayer;

import javax.sound.sampled.AudioFormat;

/**
 * Intermediate "proxy" class
 */
public abstract class AudioPlayer extends TeamspeakFastMixerSink implements IAudioPlayer {

    AudioPlayer(AudioFormat audioFormat, int bufferSizeInBytes, OpusParameters opusParameters) {
        super(audioFormat, bufferSizeInBytes, opusParameters);
    }
}
