package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.profile.event.IProfileEvent;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.framework.subject.ConfigSubject;
import de.fearnixx.jeak.service.permission.framework.subject.SubjectAccessor;
import de.fearnixx.jeak.service.permission.framework.subject.UserConfigSubject;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SubjectCache {

    private static final Logger logger = LoggerFactory.getLogger(SubjectCache.class);

    // Map of all encountered profile merges during this runtime.
    // No persistence needed as their subjects will reflect the correct UUID on the next join.
    private final Map<UUID, UUID> profileMerges = new ConcurrentHashMap<>();

    private final Map<UUID, SubjectAccessor> cachedAccessors = new ConcurrentHashMap<>();
    private final Map<UUID, LocalDateTime> cacheTimings = new ConcurrentHashMap<>();

    private final ITask cacheBuster = ITask.builder()
            .name("perms-cache-buster")
            .interval(3, TimeUnit.MINUTES)
            .runnable(this::bustCaches)
            .build();
    private final InternalPermissionProvider provider;

    @Inject
    private ITaskService taskService;

    @Inject
    private IProfileService profileService;

    @Inject
    private IBot bot;

    @Inject
    private IInjectionService injectionService;

    public SubjectCache(InternalPermissionProvider provider) {
        this.provider = provider;
    }

    public synchronized SubjectAccessor getSubject(UUID uuid) {
        // When redirected, use that UUID. Otherwise, use the given one.
        UUID realUUID = profileMerges.getOrDefault(uuid, uuid);
        cacheTimings.put(uuid, LocalDateTime.now().plusMinutes(20));
        return cachedAccessors.computeIfAbsent(realUUID, id -> makeStoredSubject(realUUID));
    }

    private SubjectAccessor makeStoredSubject(UUID uuid) {
        final Optional<IUserProfile> optProfile = profileService.getProfile(uuid);

        IConfigLoader loader = LoaderFactory.getLoader("application/json");
        File subjectFile = new File(bot.getConfigDirectory(), "permissions/" + uuid.toString() + ".json");
        FileConfig config = new FileConfig(loader, subjectFile);

        ConfigSubject subject;
        if (optProfile.isEmpty()) {
            logger.debug("Created new unspecified subject for: {}", uuid);
            subject = new ConfigSubject(uuid, config, provider);
        } else {
            logger.debug("Created new user subject for: {}", uuid);
            subject = new UserConfigSubject(optProfile.get(), config, provider);
        }
        injectionService.injectInto(subject);
        return subject;
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        taskService.scheduleTask(cacheBuster);
    }

    @Listener
    public synchronized void onProfilesMerged(IProfileEvent.IProfileMerge mergeEvent) {
        UUID from = mergeEvent.getMergeSource().getUniqueId();
        UUID into = mergeEvent.getTargetProfile().getUniqueId();

        getSubject(from).mergeFrom(getSubject(into));
        profileMerges.put(from, into);
    }

    @Listener
    public synchronized void onShutdown(IBotStateEvent.IPreShutdown event) {
        taskService.removeTask(cacheBuster);
        cachedAccessors.values().forEach(SubjectAccessor::saveIfModified);
        cachedAccessors.clear();
        provider.getIndex().saveIfModified();
    }

    private synchronized void bustCaches() {
        LocalDateTime time = LocalDateTime.now();
        List<UUID> expiredUUIDs = cacheTimings.entrySet()
                .stream()
                .filter(e -> e.getValue().isBefore(time))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        expiredUUIDs.forEach(expired -> {
            SubjectAccessor accessor = cachedAccessors.remove(expired);
            cacheTimings.remove(expired);

            if (accessor == null) {
                logger.warn("Cache miss for expired accessor? {}", expired);
            } else {
                accessor.saveIfModified();
            }
        });

        provider.getIndex().saveIfModified();
    }

    public Optional<IGroup> createGroup(String name) {
        final UUID groupUID = provider.getIndex().createGroup(name);
        return Optional.ofNullable(getSubject(groupUID));
    }

    public boolean delete(UUID subjectUUID) {
        provider.getIndex().deleteSubject(subjectUUID);
        return true;
    }
}
