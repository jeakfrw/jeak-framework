package de.fearnixx.jeak.teamspeak.voice.connection;

import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class VoiceConnectionStore implements IVoiceConnectionStore {

    private Map<String, IVoiceConnection> connectionStore = new ConcurrentHashMap<>();
    private VoiceConnectionService voiceConnectionService;

    VoiceConnectionStore(VoiceConnectionService voiceConnectionService) {
        this.voiceConnectionService = voiceConnectionService;
    }

    @Override
    public void prepareVoiceConnection(String identifier) {
        voiceConnectionService.requestVoiceConnection(
                identifier, optionalVoiceConnection -> optionalVoiceConnection.ifPresent(
                        voiceConnection -> connectionStore.put(identifier, voiceConnection)
                )
        );
    }

    @Override
    public void prepareVoiceConnection(String identifier, Consumer<IVoiceConnection> onRegistrationFinished) {
        voiceConnectionService.requestVoiceConnection(
                identifier, optionalVoiceConnection -> optionalVoiceConnection.ifPresent(
                        voiceConnection -> {
                            connectionStore.put(identifier, voiceConnection);
                            onRegistrationFinished.accept(voiceConnection);
                        }
                )
        );
    }

    @Override
    public IVoiceConnection getVoiceConnection(String identifier) {
        return optVoiceConnection(identifier).orElseThrow(
                () -> new IllegalArgumentException(
                        "The requested voice connection with the identifier " + identifier +
                                " was not registered in this pool!"
                )
        );
    }

    @Override
    public Optional<IVoiceConnection> optVoiceConnection(String identifier) {
        return Optional.ofNullable(connectionStore.get(identifier));
    }
}
