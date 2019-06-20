package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.profile.IProfileService;
import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.teamspeak.TS3UserSubject;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.TS3User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractUserService implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractUserService.class);

    @Inject
    private IDataCache dataCache;

    @Inject
    private IPermissionService permService;

    @Inject
    private IProfileService profileService;

    @Override
    public List<IClient> findClientByUniqueID(String ts3uniqueID) {
        return dataCache.getClients()
                .stream()
                .filter(client -> client.getClientUniqueID().startsWith(ts3uniqueID))
                .collect(Collectors.toList());
    }

    @Override
    public List<IClient> findClientByDBID(int ts3dbID) {
        return dataCache.getClients()
                .stream()
                .filter(client -> client.getClientDBID().equals(ts3dbID))
                .collect(Collectors.toList());
    }

    @Override
    public List<IClient> findClientByNickname(String ts3nickname) {
        return dataCache.getClients()
                .stream()
                .filter(client -> client.getNickName().toLowerCase().contains(ts3nickname.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<IClient> getClientByID(int clientId) {
        return Optional.ofNullable(dataCache.getClientMap().getOrDefault(clientId, null));
    }

    protected void applyPermissions(TS3User client) {
        String ts3uid = client.getClientUniqueID();
        UUID uuid = profileService.getProfile(ts3uid)
                .map(IUserProfile::getUniqueId)
                .orElseGet(UUID::randomUUID);
        logger.debug("Client {} got permission UUID: {}", client, uuid);

        final Integer tsSubject = client.getClientDBID();
        client.setTs3PermSubject(new TS3UserSubject(permService.getTS3Provider(), tsSubject));
        client.setFrameworkSubjectUUID(uuid);
        client.setFrwPermProvider(permService.getFrameworkProvider());
    }
}
