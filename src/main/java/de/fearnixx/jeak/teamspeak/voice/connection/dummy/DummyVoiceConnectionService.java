package de.fearnixx.jeak.teamspeak.voice.connection.dummy;

import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionService;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionStore;

import java.util.Optional;
import java.util.function.Consumer;

@FrameworkService(serviceInterface = IVoiceConnectionService.class)
public class DummyVoiceConnectionService implements IVoiceConnectionService {

    @Override
    public void requestVoiceConnection(String identifier, Consumer<Optional<IVoiceConnection>> onRequestFinished) {
        //Do nothing
    }

    @Override
    public IVoiceConnectionStore createVoiceConnectionStore() {
        return new DummyVoiceConnectionStore();
    }

    @Override
    public IVoiceConnectionStore createVoiceConnectionStore(String... identifiers) {
        return new DummyVoiceConnectionStore();
    }
}
