package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.profile.IUserProfile;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IValueHolder;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigProfile implements IUserProfile {

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
    public synchronized List<String> getTSIdentities() {
        return Collections.unmodifiableList(configRef.getRoot()
                .getNode("identities")
                .optList()
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(n -> n.optString().isPresent())
                .map(IValueHolder::asString)
                .collect(Collectors.toList()));
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

    }

    protected void save() {

    }
}
