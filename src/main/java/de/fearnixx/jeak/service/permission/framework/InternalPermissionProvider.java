package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.profile.event.IProfileEvent;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InternalPermissionProvider implements IPermissionProvider {

    public static final String SYSTEM_ID = "jeak";

    @Inject
    private IInjectionService injectionService;

    @Inject
    private IEventService eventService;

    @Inject
    private IProfileService profileSvc;

    @Inject
    @Config(category = "permissions", id = "dummy")
    private File permissionDirectory;

    private SubjectCache subjectCache = new SubjectCache();

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        eventService.registerListener(subjectCache);
        injectionService.injectInto(subjectCache);

    }

    @Override
    public Optional<IPermission> getPermission(String permSID, String clientTS3UniqueID) {
        Optional<IUserProfile> optProfile = profileSvc.getProfile(clientTS3UniqueID);

        return optProfile.map(profile -> {
            UUID profileUUID = profile.getUniqueId();
            SubjectAccessor store = subjectCache.getSubject(profileUUID);
            return store.getPermission(permSID).orElse(null);
        });
    }

    @Override
    public Optional<IPermission> getPermission(String permSID, UUID subjectUniqueID) {
        SubjectAccessor store = subjectCache.getSubject(subjectUniqueID);
        return store.getPermission(permSID);
    }

    @Override
    public void setPermission(String permSID, UUID subjectUniqueID, int value) {
        SubjectAccessor accessor = subjectCache.getSubject(subjectUniqueID);
        accessor.setPermission(permSID, value);
    }

    @Override
    public void removePermission(String permSID, UUID subjectUniqueID) {
        SubjectAccessor accessor = subjectCache.getSubject(subjectUniqueID);
        accessor.removePermission(permSID);
    }
}
