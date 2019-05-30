package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractUserService implements IUserService {

    @Inject
    private IDataCache dataCache;

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
}
