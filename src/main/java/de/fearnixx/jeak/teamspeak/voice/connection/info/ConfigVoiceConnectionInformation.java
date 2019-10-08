package de.fearnixx.jeak.teamspeak.voice.connection.info;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ConfigVoiceConnectionInformation extends AbstractVoiceConnectionInformation {

    private static final String NICKNAME_NODE = "nickname";
    private static final String IDENTITY_NODE = "identity";
    private IConfig configRef;

    private String identifier;

    public ConfigVoiceConnectionInformation(IConfig configRef, String identifier) {
        this.identifier = identifier;
        this.configRef = configRef;

        try {
            this.configRef.load();

            if (this.configRef.getRoot() == null) {
                this.configRef.createRoot();
            }

        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Could not read voice connection info config!");
        }
    }

    private IConfigNode getInfoNode() {
        final IConfigNode node = configRef.getRoot().getNode(this.identifier);

        if (node.isVirtual()) {
            node.setMap();
            save();
        }

        return node;
    }

    @Override
    public void setClientNickname(String clientNickname) {
        getInfoNode().getNode(NICKNAME_NODE).setString(clientNickname);

        save();
    }

    @Override
    public void setLocalIdentity(LocalIdentity localIdentity) {

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            localIdentity.save(outputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save identity!");
        }

        getInfoNode().getNode(IDENTITY_NODE).setString(outputStream.toString());

        save();
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public LocalIdentity getTeamspeakIdentity() {
        final String identityString = getInfoNode().getNode(IDENTITY_NODE).asString();

        try {
            return LocalIdentity.read(new ByteArrayInputStream(identityString.getBytes()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load identity!");
        }
    }

    @Override
    public String getClientNickname() {
        return getInfoNode().getNode(NICKNAME_NODE).asString();
    }

    private void save() {
        try {
            configRef.save();
        } catch (IOException e) {
            //Saving next time
        }
    }
}
