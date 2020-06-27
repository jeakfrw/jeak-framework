package de.fearnixx.jeak.teamspeak.voice.connection.dummy;

import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionStore;

import java.util.Optional;
import java.util.function.Consumer;

public class DummyVoiceConnectionStore implements IVoiceConnectionStore {

    @Override
    public void prepareVoiceConnection(String identifier) {
        //Do nothing
    }

    @Override
    public void prepareVoiceConnection(String identifier, Consumer<IVoiceConnection> onRegistrationFinished) {
        //Do nothing
    }

    @Override
    public IVoiceConnection getVoiceConnection(String identifier) {
        throw new IllegalArgumentException("Voice connection with identifier " + identifier + " is not prepared!");
    }

    @Override
    public Optional<IVoiceConnection> optVoiceConnection(String identifier) {
        return Optional.empty();
    }
}
