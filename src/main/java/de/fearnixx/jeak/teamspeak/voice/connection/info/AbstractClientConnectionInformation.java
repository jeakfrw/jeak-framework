package de.fearnixx.jeak.teamspeak.voice.connection.info;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.fearnixx.jeak.voice.connection.IClientConnectionInformation;

public abstract class AbstractClientConnectionInformation implements IClientConnectionInformation {

    public abstract void setClientNickname(String clientNickname);

    public abstract void setLocalIdentity(LocalIdentity localIdentity);

    public abstract LocalIdentity getTeamspeakIdentity();
}
