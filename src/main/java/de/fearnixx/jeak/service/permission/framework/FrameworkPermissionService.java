package de.fearnixx.jeak.service.permission.framework;

import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.permission.base.IPermission;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.lang.IConfigLoader;
import de.mlessmann.confort.config.FileConfig;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class FrameworkPermissionService implements IPermissionProvider {

    public static final String SYSTEM_ID = "jeak";

    @Inject
    private IProfileService profileSvc;

    private File permissionDirectory;

    @Override
    public Optional<IPermission> getPermission(String permSID, String clientUID) {
        Optional<IUserProfile> optProfile = profileSvc.getProfile(clientUID);
        if (!optProfile.isPresent()) {
            return Optional.empty();
        }

        UUID profileUUID = optProfile.get().getUniqueId();
        PermissionSubject subject = makeSubject(profileUUID);

        return subject.getPermission(permSID);
    }

    private PermissionSubject makeSubject(UUID uuid) {
        IConfigLoader loader = LoaderFactory.getLoader("application/json");
        File subjectFile = new File(permissionDirectory, uuid.toString() + ".json");
        FileConfig config = new FileConfig(loader, subjectFile);
        return new ConfigSubject(uuid, config);
    }
}
