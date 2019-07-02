package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.permission.framework.commands.CreateGroupCommand;
import de.fearnixx.jeak.service.permission.framework.commands.GrantPermissionCommand;
import de.fearnixx.jeak.service.permission.framework.commands.LinkGroupCommand;
import de.fearnixx.jeak.service.permission.framework.index.ConfigIndex;
import de.fearnixx.jeak.service.permission.framework.index.SubjectIndex;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InternalPermissionProvider implements IPermissionProvider {

    public static final String SYSTEM_ID = "jeak";

    @Inject
    private IBot bot;

    @Inject
    private IInjectionService injectionService;

    @Inject
    private IEventService eventService;

    @Inject
    private ICommandService commandService;

    private final SubjectIndex subjectIndex = new ConfigIndex();
    private SubjectCache subjectCache = new SubjectCache(this);

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        IConfigLoader loader = LoaderFactory.getLoader("application/json");
        FileConfig config = new FileConfig(loader, new File(bot.getConfigDirectory(), "permissions/_index.json"));
        ((ConfigIndex) subjectIndex).setConfig(config);
        ((ConfigIndex) subjectIndex).load();
        eventService.registerListener(subjectCache);
        injectionService.injectInto(subjectCache);

        final CreateGroupCommand crGrpCommand = new CreateGroupCommand();
        final LinkGroupCommand lnkGrpCommand = new LinkGroupCommand();
        final GrantPermissionCommand grntPermCommand = new GrantPermissionCommand();
        injectionService.injectInto(crGrpCommand);
        injectionService.injectInto(lnkGrpCommand);
        injectionService.injectInto(grntPermCommand);
        commandService.registerCommand("perm-group-create", crGrpCommand);
        commandService.registerCommand("perm-group-link", lnkGrpCommand);
        commandService.registerCommand("perm-group-grant", grntPermCommand);
    }

    @Override
    public List<IGroup> getGroupsLinkedToServerGroup(int serverGroupId) {
        return subjectIndex.getGroupsLinkedTo(serverGroupId)
                .stream()
                .map(this::getSubject)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(IGroup.class::cast)
                .collect(Collectors.toUnmodifiableList());
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

    @Override
    public Optional<IGroup> findGroupByName(String name) {
        Optional<UUID> optGroupUUID = subjectIndex.findGroupByName(name);
        return optGroupUUID.map(this::getSubject)
                .map(Optional::get)
                .map(IGroup.class::cast);
    }

    @Override
    public Optional<IGroup> createParent(String name) {
        return subjectCache.createGroup(name);
    }

    @Override
    public boolean deleteSubject(UUID subjectUUID) {
        return subjectCache.delete(subjectUUID);
    }

    public SubjectCache getCache() {
        return subjectCache;
    }

    public SubjectIndex getIndex() {
        return subjectIndex;
    }
}
