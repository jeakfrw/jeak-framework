package de.fearnixx.jeak.teamspeak.voice.connection.info;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionInformation;

import java.util.Optional;

public abstract class AbstractVoiceConnectionInformation implements IVoiceConnectionInformation {

    private Integer clientId;

    public abstract void setClientNickname(String clientNickname);

    public abstract void setClientDescription(String clientDescription);

    public abstract void setLocalIdentity(LocalIdentity localIdentity);

    public abstract LocalIdentity getTeamspeakIdentity();

    @Override
    public String getClientUniqueId() {
        return getTeamspeakIdentity().getUid().toBase64();
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    @Override
    public Optional<Integer> optClientId() {
        return Optional.ofNullable(clientId);
    }

    @Override
    public Integer getClientId() {
        return optClientId().orElseThrow(() -> new IllegalStateException(
                "The voice connection with the identifier "
                        + getIdentifier() +
                        " does not provide a client id, since it is probably not connected to the server!")
        );
    }
}
