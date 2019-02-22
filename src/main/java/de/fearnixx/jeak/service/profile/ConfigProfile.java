package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.IValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigProfile implements IUserProfile {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProfile.class);

    private final IConfig configRef;
    private final UUID uuid;

    public ConfigProfile(IConfig configRef, UUID uuid) {
        this.configRef = configRef;
        this.uuid = uuid;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public synchronized List<IUserIdentity> getTSIdentities() {
        return getLinkedIdentities(IUserIdentity.SERVICE_TEAMSPEAK);
    }

    @Override
    public List<IUserIdentity> getLinkedIdentities(String serviceId) {
        return Collections.unmodifiableList(configRef.getRoot()
                .getNode("identities", serviceId)
                .optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(n -> n.optString().isPresent())
                .map(IValueHolder::asString)
                .map(identity -> new UserIdentity(serviceId, identity))
                .collect(Collectors.toList()));
    }

    @Override
    public List<IUserIdentity> getLinkedIdentities() {
        List<IUserIdentity> results = new LinkedList<>();

        configRef.getRoot().getNode("identities")
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((service, identities) -> {
                    identities.optList()
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .filter(n -> n.optString().isPresent())
                            .map(IValueHolder::asString)
                            .map(identity -> new UserIdentity(service, identity))
                            .forEach(results::add);
                });
        return results;
    }

    @Override
    public synchronized Optional<String> getOption(String optionId) {
        Objects.requireNonNull(optionId, "Option id may not be null!");
        return configRef.getRoot().getNode("options", optionId).optString();
    }

    @Override
    public String getOption(String optionId, String def) {
        return getOption(optionId).orElse(def);
    }

    @Override
    public void setOption(String optionId, String value) {
        Objects.requireNonNull(optionId, "Option id may not be null!");

        if (value == null) {
            removeOption(optionId);
        } else {
            configRef.getRoot().getNode("options", optionId).setString(value);
        }
    }

    @Override
    public void removeOption(String optionId) {
        final IConfigNode element = configRef.getRoot()
                .getNode("options")
                .remove(optionId);

        if (!element.isVirtual()) {
            save();
        }
    }

    protected void save() {
        synchronized (configRef) {
            try {
                configRef.save();
            } catch (IOException e) {
                logger.warn("Failed to save profile \"{}\"", uuid, e);
            }
        }
    }
}
