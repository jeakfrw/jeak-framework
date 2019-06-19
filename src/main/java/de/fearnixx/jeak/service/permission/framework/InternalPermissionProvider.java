package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.fearnixx.jeak.service.permission.base.ISubject;

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

    private SubjectCache subjectCache = new SubjectCache();

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        eventService.registerListener(subjectCache);
        injectionService.injectInto(subjectCache);
    }

    @Override
    public Optional<ISubject> getSubject(UUID subjectUUID) {
        return Optional.of(subjectCache.getSubject(subjectUUID));
    }

    @Override
    public List<IGroup> getParentsOf(UUID subjectUniqueID) {
        return subjectCache.getSubject(subjectUniqueID).getParents();
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
    public boolean setPermission(String permSID, UUID subjectUniqueID, int value) {
        subjectCache.getSubject(subjectUniqueID).setPermission(permSID, value);
        return true;
    }

    @Override
    public boolean removePermission(String permSID, UUID subjectUniqueID) {
        subjectCache.getSubject(subjectUniqueID).removePermission(permSID);
        return true;
    }
}
