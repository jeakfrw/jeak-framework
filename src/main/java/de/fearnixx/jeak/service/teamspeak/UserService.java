package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.IServiceManager;
import de.fearnixx.jeak.service.database.IDatabaseService;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Small loader around the user service that decides whether or not to use direct DB access or queries.
 */
@FrameworkService(serviceInterface = IUserService.class)
public class UserService implements IUserService {

    public static final String PERSISTENCE_UNIT_NAME = "teamspeak";
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private IDatabaseService databaseService;

    @Inject
    private IEventService eventService;

    @Inject
    private IInjectionService injectionService;

    private IUserService serviceImplementation;

    @Listener
    public void onPreInitialize(IBotStateEvent.IPreInitializeEvent event) {
        Optional<IPersistenceUnit> optUnit = databaseService.getPersistenceUnit(PERSISTENCE_UNIT_NAME);
        if (optUnit.isPresent()) {
            logger.info("Persistence unit available! Using faster db-supported algorithm.");
            serviceImplementation = new DBUserService(optUnit.get());
        } else {
            logger.warn("Persistence unit not available. Expect degraded performance in offline user retrieval.");
            logger.info("Please consider registering persistence unit \"{}\" to enable direct database access.", PERSISTENCE_UNIT_NAME);
            serviceImplementation = new QueryUserService();
        }

        eventService.registerListener(serviceImplementation);
        injectionService.injectInto(serviceImplementation);
    }

    @Override
    public List<IUser> findUserByUniqueID(String ts3uniqueID) {
        return serviceImplementation.findUserByUniqueID(ts3uniqueID);
    }

    @Override
    public List<IUser> findUserByDBID(int ts3dbID) {
        return serviceImplementation.findUserByDBID(ts3dbID);
    }

    @Override
    public List<IUser> findUserByNickname(String ts3nickname) {
        return serviceImplementation.findUserByNickname(ts3nickname);
    }

    @Override
    public List<IClient> findClientByUniqueID(String ts3uniqueID) {
        return serviceImplementation.findClientByUniqueID(ts3uniqueID);
    }

    @Override
    public List<IClient> findClientByDBID(int ts3dbID) {
        return serviceImplementation.findClientByDBID(ts3dbID);
    }

    @Override
    public List<IClient> findClientByNickname(String ts3nickname) {
        return serviceImplementation.findClientByNickname(ts3nickname);
    }

    @Override
    public Optional<IClient> getClientByID(int clientId) {
        return serviceImplementation.getClientByID(clientId);
    }
}
