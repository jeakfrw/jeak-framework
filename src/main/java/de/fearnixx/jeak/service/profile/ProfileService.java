package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.FrameworkService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@FrameworkService(serviceInterface = IProfileService.class)
public class ProfileService implements IProfileService {

    private static final Integer PROFILE_CACHE_MINUTES = Main.getProperty("jeak.profileCache.minutesTTL", 10);

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final File profileDirectory;
    private final IConfig indexConfig;
    private final IConfigLoader configLoader = LoaderFactory.getLoader("application/json");
    private final List<ConfigProfile> profileSaveQueue = new LinkedList<>();
    private final Map<UUID, ConfigProfile> profileCache = new ConcurrentHashMap<>();
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

    private synchronized void saveIndex() {
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

    private synchronized void saveProfiles() {
        if (!profileSaveQueue.isEmpty()) {
            logger.debug("Flushing profiles and index.");
            profileSaveQueue.forEach(ConfigProfile::save);
            profileSaveQueue.clear();
        }

        LocalDateTime cacheRemovalThreshold = LocalDateTime.now().plusMinutes(-PROFILE_CACHE_MINUTES);
        profileCache.entrySet()
                .removeIf(e -> e.getValue().getLastAccess().isBefore(cacheRemovalThreshold));

        saveIndex();
    }

    private synchronized void onProfileModified(ConfigProfile configProfile) {
        if (!profileSaveQueue.contains(configProfile)) {
            profileSaveQueue.add(configProfile);
        }
    }

    @Override
    public synchronized Optional<IUserProfile> getProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "Lookup UUID may not be null!");

        if (!indexConfig.getRoot().getNode(uuid.toString()).isVirtual()) {
            return Optional.ofNullable(retrieveUserProfile(uuid, null).orElse(null));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IUserProfile> getProfile(String ts3Identity) {
        final Optional<UUID> optUUID = lookupUUID(ts3Identity);
        return optUUID.map(uuid -> retrieveUserProfile(uuid, null).orElse(null));
    }

    @Override
    public Optional<IUserProfile> getOrCreateProfile(String ts3Identity) {
        Objects.requireNonNull(ts3Identity, "Lookup identity may not be null!");
        final Optional<UUID> optUUID = lookupUUID(ts3Identity);
        final UUID uuid = optUUID.orElseGet(UUID::randomUUID);
        final ConfigProfile profile = retrieveUserProfile(uuid, ts3Identity)
                .orElseThrow(() -> new IllegalStateException("Failed to create profile: " + uuid));
        if (optUUID.isEmpty()) {
            addToIndex(ts3Identity, uuid);
        }
        return Optional.ofNullable(profile);
    }

    private synchronized void addToIndex(String ts3Identity, UUID uuid) {
        IConfigNode listEntry = indexConfig.getRoot().createNewInstance();
        listEntry.setString(ts3Identity);
        indexConfig.getRoot().getNode(uuid.toString(), IUserIdentity.SERVICE_TEAMSPEAK).append(listEntry);
        indexModified = true;
    }

    private synchronized Optional<UUID> lookupUUID(String ts3identity) {
        Objects.requireNonNull(ts3identity, "Cannot look up null identity!");
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
                            if (!e.getValue().isVirtual()) {
                                logger.warn("UUID index is not a list! [{}]", e.getKey());
                            }
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

        return Optional.empty();
    }

    /**
     * Returns the profile associated with the provided {@link UUID}.
     * Only creates new profiles when {@code createIfAbsent} is set.
     * The cached profile is returned, when available.
     */
    private Optional<ConfigProfile> retrieveUserProfile(UUID uuid, String forTs3Identity) {
        ConfigProfile profile;
        profile = getProfileFromCache(uuid);

        if (profile != null) {
            profile.updateAccessTimestamp();
            return Optional.of(profile);

        } else {
            Optional<ConfigProfile> optProfile = makeUserProfile(uuid, forTs3Identity);
            optProfile.ifPresent(this::cacheProfile);
            return optProfile;
        }
    }

    private synchronized ConfigProfile getProfileFromCache(UUID uuid) {
        return profileCache.getOrDefault(uuid, null);
    }

    private synchronized void cacheProfile(ConfigProfile profile) {
        profileCache.put(profile.getUniqueId(), profile);
    }

    private Optional<ConfigProfile> makeUserProfile(UUID uuid, String ts3Identity) {
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

        if (isNew && profile != null) {
            if (ts3Identity == null) {
                throw new IllegalStateException("Profiles cannot be created without an initial identity!");
            }
            profile.unsafeAddIdentity(new UserIdentity(IUserIdentity.SERVICE_TEAMSPEAK, ts3Identity));
            onProfileModified(profile);
            logger.debug("Created profile: {}", uuid);
            ProfileEvent.ProfileCreatedEvent createdEvent = new ProfileEvent.ProfileCreatedEvent();
            createdEvent.setTargetProfile(profile);
            eventService.fireEvent(createdEvent);
        }

        return Optional.ofNullable(profile);
    }

    private synchronized Optional<ConfigProfile> getProfileFromModificationCache(UUID uuid) {
        return profileSaveQueue.stream()
                .filter(profile -> profile.getUniqueId().equals(uuid))
                .findFirst();
    }

    private File getProfileFSRef(UUID uuid) {
        final String fileName = uuid.toString() + ".json";
        return new File(profileDirectory, fileName);
    }

    @Override
    public void mergeProfiles(UUID into, UUID other) {
        ConfigProfile intoProfile = retrieveUserProfile(into, null)
                .orElseThrow(() -> new IllegalArgumentException("No profile for target UUID: " + into));
        ConfigProfile fromProfile = retrieveUserProfile(other, null)
                .orElseThrow(() -> new IllegalArgumentException("No profile for source UUID: " + other));

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
    public synchronized void deleteProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "Cannot delete null profile!");

        indexConfig.getRoot().remove(uuid.toString());
        indexModified = true;
        profileCache.remove(uuid);
        profileSaveQueue.removeIf(profile -> profile.getUniqueId().equals(uuid));

        final File profileFile = getProfileFSRef(uuid);
        try {
            if (Files.exists(profileFile.toPath())) {
                Files.delete(profileFile.toPath());
            }
            ProfileEvent.ProfileDeletedEvent deletedEvent = new ProfileEvent.ProfileDeletedEvent();
            deletedEvent.setProfileUUID(uuid);
            eventService.fireEvent(deletedEvent);
        } catch (IOException e) {
            logger.error("Failed to delete profile {} !", profileFile.getPath(), e);
        }
    }
}
