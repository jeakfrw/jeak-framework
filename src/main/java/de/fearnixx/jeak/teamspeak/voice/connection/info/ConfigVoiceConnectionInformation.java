package de.fearnixx.jeak.teamspeak.voice.connection.info;

import com.github.manevolent.ts3j.identity.LocalIdentity;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ConfigVoiceConnectionInformation extends AbstractVoiceConnectionInformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigVoiceConnectionInformation.class);

    private static final String IDENTITY_NODE = "identity";
    private static final String NICKNAME_NODE = "nickname";
    private static final String DESCRIPTION_NODE = "description";
    private final IConfig configRef;

    private final String identifier;

    public ConfigVoiceConnectionInformation(IConfig configRef, String identifier) {
        this.identifier = identifier;
        this.configRef = configRef;

        try {
            this.configRef.load();

            if (this.configRef.getRoot() == null) {
                this.configRef.createRoot();
            }

        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Could not read voice connection info config!", e);
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
    public void setLocalIdentity(LocalIdentity localIdentity) {

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            localIdentity.save(outputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save identity!", e);
        }

        getInfoNode().getNode(IDENTITY_NODE).setString(outputStream.toString());
        save();
    }

    @Override
    public void setClientNickname(String clientNickname) {
        getInfoNode().getNode(NICKNAME_NODE).setString(clientNickname);
        save();
    }

    @Override
    public void setClientDescription(String clientDescription) {
        getInfoNode().getNode(DESCRIPTION_NODE).setString(clientDescription);
        save();
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public LocalIdentity getTeamspeakIdentity() {
        final IConfigNode node = getInfoNode().getNode(IDENTITY_NODE);

        if (node.isVirtual()) {
            return null;
        }

        final String identityString = node.asString();

        try {
            return LocalIdentity.read(new ByteArrayInputStream(identityString.getBytes()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load identity!", e);
        }
    }

    @Override
    public String getClientNickname() {
        final IConfigNode node = getInfoNode().getNode(NICKNAME_NODE);

        if (node.isVirtual()) {
            return identifier;
        }

        return node.asString();
    }

    @Override
    public String getClientDescription() {
        final IConfigNode node = getInfoNode().getNode(DESCRIPTION_NODE);

        if (node.isVirtual()) {
            return "";
        }

        return node.asString();
    }

    private void save() {
        try {
            configRef.save();
        } catch (IOException e) {
            //Saving next time
            LOGGER.warn("An exception occurred while trying to save the config for identifier {}", identifier, e);
        }
    }
}
