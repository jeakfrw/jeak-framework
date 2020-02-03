package de.fearnixx.jeak.teamspeak.voice.sound;

import de.fearnixx.jeak.voice.sound.AudioType;

public class WebRadioPlayer extends Mp3AudioPlayer {

    @Override
    public AudioType getAudioType() {
        return AudioType.WEBRADIO;
    }
}
