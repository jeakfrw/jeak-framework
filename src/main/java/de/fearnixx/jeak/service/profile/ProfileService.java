package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.profile.event.ProfileEvent;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
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
import java.util.concurrent.TimeUnit;

public class ProfileService implements IProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final File profileDirectory;
    private final IConfig indexConfig;
    private final IConfigLoader configLoader = LoaderFactory.getLoader("application/json");
    private final List<ConfigProfile> profileSaveQueue = new LinkedList<>();
    private boolean indexModified = false;

    @Inject
    private ITaskService taskService;
    private ITask profileSaveTask = ITask.builder()
            .name("frw-profile-save")
            .interval(1, TimeUnit.MINUTES)
            .runnable(this::saveProfiles)
            .build();

    @Inject
    private IEventService eventService;

    public ProfileService(File profileDirectory) {
        Objects.requireNonNull(profileDirectory, "Profile directory may not be null!");
        this.profileDirectory = profileDirectory;

        final File indexFile = new File(profileDirectory, "_index.json");
        this.indexConfig = new FileConfig(configLoader, indexFile);
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        try {
            indexConfig.load();
        } catch (IOException | ParseException e) {
            logger.error("Failed to load profile index!", e);
            event.cancel();
            return;
        }

        if (indexConfig.getRoot() == null) {
            logger.info("Initializing empty profile index.");
            indexConfig.createRoot();
            indexConfig.getRoot().setMap();
            saveIndex();
        }

        taskService.scheduleTask(profileSaveTask);
    }

    @Listener
    public void onShutdown(IBotStateEvent.IPreShutdown event) {
        saveIndex();
    }

    private void saveIndex() {
        synchronized (indexConfig) {
            if (indexModified) {
                try {
                    logger.debug("Saving index.");
                    indexConfig.save();
                    indexModified = false;
                } catch (IOException e) {
                    logger.warn("Failed to save profile index!", e);
                }
            }
        }
    }

    private void saveProfiles() {
        synchronized (profileSaveQueue) {
            if (!profileSaveQueue.isEmpty()) {
                logger.debug("Flushing profiles and index.");
                profileSaveQueue.forEach(ConfigProfile::save);
                profileSaveQueue.clear();
            }
            saveIndex();
        }
    }

    private void onProfileModified(ConfigProfile configProfile) {
        synchronized (profileSaveQueue) {
            if (!profileSaveQueue.contains(configProfile)) {
                profileSaveQueue.add(configProfile);
            }
        }
    }

    @Override
    public Optional<IUserProfile> getProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "Lookup UUID may not be null!");
        return Optional.ofNullable(lookupProfileFromFS(uuid).orElse(null));
    }

    @Override
    public Optional<IUserProfile> getProfile(String ts3Identity) {
        final Optional<UUID> optUUID = lookupUUIDFromFS(ts3Identity);
        return optUUID.map(uuid -> lookupProfileFromFS(uuid).orElse(null));
    }

    @Override
    public Optional<IUserProfile> getOrCreateProfile(String ts3Identity) {
        Objects.requireNonNull(ts3Identity, "Lookup identity may not be null!");

        final Optional<UUID> optUUID = lookupUUIDFromFS(ts3Identity);
        UUID uuid = optUUID.orElseGet(UUID::randomUUID);

        if (!optUUID.isPresent()) {
            addToIndex(ts3Identity, uuid);
        }

        return Optional.ofNullable(makeUserProfile(uuid).orElse(null));
    }

    private void addToIndex(String ts3Identity, UUID uuid) {
        synchronized (indexConfig) {
            IConfigNode listEntry = indexConfig.getRoot().createNewInstance();
            listEntry.setString(ts3Identity);
            indexConfig.getRoot().getNode(uuid.toString(), IUserIdentity.SERVICE_TEAMSPEAK).append(listEntry);
            indexModified = true;
        }
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
                            .getNode(IUserIdentity.SERVICE_TEAMSPEAK)
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


    private Optional<ConfigProfile> lookupProfileFromFS(UUID uuid) {
        final File profileFile = getProfileFSRef(uuid);

        if (profileFile.isFile() && profileFile.exists()) {
            return makeUserProfile(uuid);
        } else {
            return Optional.empty();
        }
    }

    private Optional<ConfigProfile> makeUserProfile(UUID uuid) {
        final File profileFile = getProfileFSRef(uuid);
        final FileConfig profileConfig = new FileConfig(configLoader, profileFile);
        final boolean isNew = !profileFile.isFile();

        ConfigProfile profile = null;
        try {
            profileConfig.load();

            if (profileConfig.getRoot().isVirtual()) {
                profileConfig.getRoot().setMap();
            }

            profile = new ConfigProfile(profileConfig, uuid);
            profile.setModificationListener(this::onProfileModified);
        } catch (ParseException | IOException e) {
            logger.warn("Failed to load profile {}!", profileFile.getPath(), e);
        }

        if (isNew) {
            onProfileModified(profile);
            ProfileEvent.ProfileCreatedEvent createdEvent = new ProfileEvent.ProfileCreatedEvent();
            createdEvent.setTargetProfile(profile);
            eventService.fireEvent(createdEvent);
        }

        return Optional.ofNullable(profile);
    }

    private File getProfileFSRef(UUID uuid) {
        final String fileName = uuid.toString() + ".json";
        return new File(profileDirectory, fileName);
    }

    @Override
    public void mergeProfiles(UUID into, UUID other) {
        ConfigProfile intoProfile = lookupProfileFromFS(into).orElseThrow(
                () -> new IllegalArgumentException("No profile for target UUID: " + into));
        ConfigProfile fromProfile = lookupProfileFromFS(other).orElseThrow(
                () -> new IllegalArgumentException("No profile for source UUID: " + other));

        fromProfile.getLinkedIdentities()
                .forEach(intoProfile::unsafeAddIdentity);
        fromProfile.getOptions()
                .forEach(intoProfile::unsafeSetOption);
        onProfileModified(intoProfile);

        ProfileEvent.ProfileMergeEvent mergeEvent = new ProfileEvent.ProfileMergeEvent();
        mergeEvent.setTargetProfile(intoProfile);
        mergeEvent.setMergeSource(fromProfile);
        eventService.fireEvent(mergeEvent);

        // De-register modification listener to prevent inconsistencies
        // when somebody sill manipulates that profile in the async event!
        fromProfile.setModificationListener(null);
        deleteProfile(other);
    }

    @Override
    public void deleteProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "Cannot delete null profile!");

        synchronized (indexConfig) {
            final File profileFile = getProfileFSRef(uuid);

            synchronized (profileSaveQueue) {
                profileSaveQueue.removeIf(profile -> profile.getUniqueId().equals(uuid));
            }
            indexConfig.getRoot().remove(uuid.toString());
            indexModified = true;

            try {
                Files.delete(profileFile.toPath());
                ProfileEvent.ProfileDeletedEvent deletedEvent = new ProfileEvent.ProfileDeletedEvent();
                deletedEvent.setProfileUUID(uuid);
                eventService.fireEvent(deletedEvent);
            } catch (IOException e) {
                logger.error("Failed to delete profile {} !", profileFile.getPath(), e);
            }
        }
    }
}
