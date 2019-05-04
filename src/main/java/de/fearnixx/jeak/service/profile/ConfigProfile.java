package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.IValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConfigProfile implements IUserProfile {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProfile.class);

    private LocalDateTime lastAccess = LocalDateTime.now();
    private final IConfig configRef;
    private final UUID uuid;
    private Consumer<ConfigProfile> modificationListener;

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
    public synchronized List<IUserIdentity> getLinkedIdentities(String serviceId) {
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
    public synchronized List<IUserIdentity> getLinkedIdentities() {
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
    public synchronized void setOption(String optionId, String value) {
        Objects.requireNonNull(optionId, "Option id may not be null!");

        if (value == null) {
            removeOption(optionId);
        } else {
            configRef.getRoot().getNode("options", optionId).setString(value);
            notifyModification();
        }
    }

    @Override
    public synchronized void removeOption(String optionId) {
        final IConfigNode element = configRef.getRoot()
                .getNode("options")
                .remove(optionId);

        if (!element.isVirtual()) {
            notifyModification();
        }
    }

    @Override
    public synchronized Map<String, String> getOptions() {
        Map<String, String> copy = new HashMap<>();

        configRef.getRoot()
                .getNode("options")
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((k, v) -> {
                    String value = v.asString();
                    copy.put(k, value);
                });

        return Collections.unmodifiableMap(copy);
    }

    protected synchronized void save() {
        synchronized (configRef) {
            try {
                configRef.save();
            } catch (IOException e) {
                logger.warn("Failed to save profile \"{}\"", uuid, e);
            }
        }
    }

    public void setModificationListener(Consumer<ConfigProfile> modificationListener) {
        this.modificationListener = modificationListener;
    }

    private void notifyModification() {
        if (this.modificationListener != null) {
            this.modificationListener.accept(this);
        }
    }

    synchronized void unsafeAddIdentity(IUserIdentity id) {
        IConfigNode serviceNode = configRef.getRoot().getNode("identities", id.serviceId());
        boolean containsId = serviceNode.optList().orElseGet(Collections::emptyList)
                .stream()
                .map(IValueHolder::asString)
                .noneMatch(identity -> identity.equals(id.identity()));
        if (containsId) {
            IConfigNode listEntry = configRef.getRoot().createNewInstance();
            listEntry.setString(id.identity());
            serviceNode.append(listEntry);
        }

        // No modification notification.
        // This is an internal modification and only allowed to the service itself.
    }

    synchronized void unsafeSetOption(String optionId, String optionValue) {
        configRef.getRoot().getNode("options", optionId).setString(optionValue);

        // No modification notification.
        // This is an internal modification and only allowed to the service itself.
    }

    synchronized void updateAccessTimestamp() {
        lastAccess = LocalDateTime.now();
    }

    synchronized LocalDateTime getLastAccess() {
        return lastAccess;
    }
}
