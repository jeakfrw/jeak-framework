package de.fearnixx.jeak.teamspeak.voice.connection;

import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionPool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class VoiceConnectionPool implements IVoiceConnectionPool {

    private Map<String, IVoiceConnection> connectionPool = new ConcurrentHashMap<>();
    private VoiceConnectionService voiceConnectionService;

    VoiceConnectionPool(VoiceConnectionService voiceConnectionService) {
        this.voiceConnectionService = voiceConnectionService;
    }

    @Override
    public void registerVoiceConnection(String identifier) {
        voiceConnectionService.requestVoiceConnection(
                identifier, optionalVoiceConnection -> optionalVoiceConnection.ifPresent(
                        voiceConnection -> connectionPool.put(identifier, voiceConnection)
                )
        );
    }

    @Override
    public void registerVoiceConnection(String identifier, Consumer<IVoiceConnection> onRegistrationFinished) {
        voiceConnectionService.requestVoiceConnection(
                identifier, optionalVoiceConnection -> optionalVoiceConnection.ifPresent(
                        voiceConnection -> {
                            connectionPool.put(identifier, voiceConnection);
                            onRegistrationFinished.accept(voiceConnection);
                        }
                )
        );
    }

    @Override
    public IVoiceConnection getVoiceConnection(String identifier) {
        return optVoiceConnection(identifier).orElseThrow(
                () -> new IllegalArgumentException("The requested voice connection was not registered in this pool!")
        );
    }

    @Override
    public Optional<IVoiceConnection> optVoiceConnection(String identifier) {
        return Optional.ofNullable(connectionPool.get(identifier));
    }
}
