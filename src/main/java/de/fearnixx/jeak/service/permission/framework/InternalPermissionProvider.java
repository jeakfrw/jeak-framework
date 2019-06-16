package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            return subjectCache.getSubject(profileUUID).getPermission(permSID).orElse(null);
        });
    }

    @Override
    public Optional<IPermission> getPermission(String permSID, UUID subjectUniqueID) {
        return subjectCache.getSubject(subjectUniqueID).getPermission(permSID);
    }

    @Override
    public List<IPermission> listPermissions(UUID uniqueID) {
        return subjectCache.getSubject(uniqueID).getPermissions();
    }

    @Override
    public void setPermission(String permSID, UUID subjectUniqueID, int value) {
        subjectCache.getSubject(subjectUniqueID).setPermission(permSID, value);
    }

    @Override
    public void removePermission(String permSID, UUID subjectUniqueID) {
        subjectCache.getSubject(subjectUniqueID).removePermission(permSID);
    }
}
