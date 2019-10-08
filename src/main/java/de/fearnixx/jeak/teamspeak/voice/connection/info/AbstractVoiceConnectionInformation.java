package de.fearnixx.jeak.teamspeak.voice.connection.info;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionInformation;

public abstract class AbstractVoiceConnectionInformation implements IVoiceConnectionInformation {

    public abstract void setClientNickname(String clientNickname);

    public abstract void setLocalIdentity(LocalIdentity localIdentity);

    public abstract LocalIdentity getTeamspeakIdentity();
}
