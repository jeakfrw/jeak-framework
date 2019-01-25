package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ProfileService implements IProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final File profileDirectory;
    private final IConfig indexConfig;
    public final IConfigLoader configLoader = LoaderFactory.getLoader("application/json");

    public ProfileService(File profileDirectory) {
        Objects.requireNonNull(profileDirectory, "Profile directory may not be null!");
        this.profileDirectory = profileDirectory;

        final File indexFile = new File(profileDirectory, "_index.json");
        this.indexConfig = new FileConfig(configLoader, indexFile);
    }

    @Override
    public Optional<IUserProfile> getProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "Lookup UUID may not be null!");

        return lookupProfileFromFS(uuid);
    }

    @Override
    public Optional<IUserProfile> getProfile(String ts3Identity) {
        final Optional<UUID> optUUID = lookupUUIDFromFS(ts3Identity);
        if (optUUID.isPresent()) {
            return lookupProfileFromFS(optUUID.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IUserProfile> getOrCreateProfile(String ts3Identity) {
        Objects.requireNonNull(ts3Identity, "Lookup identity may not be null!");

        final Optional<UUID> optUUID = lookupUUIDFromFS(ts3Identity);
        UUID uuid = optUUID.orElseGet(UUID::randomUUID);
        return makeUserProfile(uuid);
    }

    private Optional<UUID> lookupUUIDFromFS(String ts3identity) {
        Objects.requireNonNull(ts3identity, "Cannot look up null identity!");

        synchronized (indexConfig) {
            final IConfigNode index = indexConfig.getRoot();

            // Index: Map<UUID, List<TS3Identity>>
            final Optional<String> optUUIDStr = index.optMap()
                    .orElseGet(() -> {
                        logger.debug("No UUIDs have been indexed yet.");
                        return Collections.emptyMap();
                    })
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue()
                            .optList()
                            .orElseGet(() -> {
                                logger.warn("UUID index is not a list! [{}]", e.getKey());
                                return Collections.emptyList();
                            })
                            .stream()
                            .map(n -> n.optString(null))
                            .anyMatch(ts3identity::equals))
                    .map(Map.Entry::getKey)
                    .findAny();

            if (optUUIDStr.isPresent()) {
                try {
                    return Optional.of(UUID.fromString(optUUIDStr.get()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Index lookup returned an invalid UUID! Index corrupted?", e);
                }
            }
        }

        return Optional.empty();
    }


    private Optional<IUserProfile> lookupProfileFromFS(UUID uuid) {
        final File profileFile = getProfileFSRef(uuid);

        if (profileFile.isFile() && profileFile.exists()) {
            return makeUserProfile(uuid);
        } else {
            return Optional.empty();
        }
    }

    private Optional<IUserProfile> makeUserProfile(UUID uuid) {
        final File profileFile = getProfileFSRef(uuid);
        final FileConfig profileConfig = new FileConfig(configLoader, profileFile);

        IUserProfile profile = null;
        try {
            profileConfig.load();
            profile = new ConfigProfile(profileConfig, uuid);
        } catch (ParseException | IOException e) {
            logger.warn("Failed to load profile {}!", profileFile.getPath(), e);
        }

        return Optional.ofNullable(profile);
    }

    private File getProfileFSRef(UUID uuid) {
        final String fileName = uuid.toString() + ".json";
        return new File(profileDirectory, fileName);
    }

    @Override
    public void mergeProfiles(UUID into, UUID other) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "Cannot delete null profile!");

        synchronized (indexConfig) {
            final File profileFile = getProfileFSRef(uuid);
            try {
                Files.delete(profileFile.toPath());
            } catch (IOException e) {
                logger.error("Failed to delete profile {} !", profileFile.getPath(), e);
            }
        }
    }
}
