package de.fearnixx.jeak.teamspeak.voice.sound;

import de.fearnixx.jeak.teamspeak.voice.sound.opus.OpusParameters;
import de.fearnixx.jeak.voice.sound.IAudioPlayer;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public abstract class AudioPlayer extends TeamspeakFastMixerSink implements IAudioPlayer {

    AudioPlayer(AudioFormat audioFormat, int bufferSizeInBytes, OpusParameters opusParameters) {
        super(audioFormat, bufferSizeInBytes, opusParameters);
    }

    @Override
    public void setAudioFile(File parentDir, String filename) throws FileNotFoundException {
        String filepath = filename;
        if (!filepath.toLowerCase().endsWith(getFilenameExtension())) {
            filepath += "." + getFilenameExtension();
        }

        setAudioStream(new FileInputStream(new File(parentDir, filepath)));
    }

    @Override
    public void setAudioFile(File audioFile) throws FileNotFoundException {
        setAudioStream(new FileInputStream(audioFile));
    }

    @Override
    public void setAudioFile(Path audioFilePath) throws FileNotFoundException {
        setAudioStream(new FileInputStream(audioFilePath.toFile()));
    }

    abstract String getFilenameExtension();
}
