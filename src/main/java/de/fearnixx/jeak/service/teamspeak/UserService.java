package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.database.IDatabaseService;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Small loader around the user service that decides whether or not to use direct DB access or queries.
 */
@FrameworkService(serviceInterface = IUserService.class)
public class UserService implements IUserService {

    public static final String PERSISTENCE_UNIT_NAME = "teamspeak";
    private static final int USR_CACHE_TTL = Main.getProperty("jeak.cache.keepUsersSecs", 30);
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private IDatabaseService databaseService;

    @Inject
    private IEventService eventService;

    @Inject
    private IInjectionService injectionService;

    private final List<CachedUserResult> userCache = new CopyOnWriteArrayList<>();

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
        return computeIfNotCached(
                u -> u.getClientUniqueID().equals(ts3uniqueID),
                () -> serviceImplementation.findUserByUniqueID(ts3uniqueID),
                "uid:" + ts3uniqueID
        );
    }

    @Override
    public List<IUser> findUserByDBID(int ts3dbID) {
        return computeIfNotCached(
                u -> u.getClientDBID().equals(ts3dbID),
                () -> serviceImplementation.findUserByDBID(ts3dbID),
                "dbid:" + ts3dbID
        );
    }

    @Override
    public List<IUser> findUserByNickname(String ts3nickname) {
        return computeIfNotCached(
                u -> u.getNickName().contains(ts3nickname),
                () -> serviceImplementation.findUserByNickname(ts3nickname),
                "nick:" + ts3nickname
        );
    }

    private List<IUser> computeIfNotCached(Predicate<IUser> matchBy, Supplier<List<IUser>> getBy, String searchHint) {
        synchronized (userCache) {
            final LocalDateTime now = LocalDateTime.now();
            userCache.removeIf(entry -> entry.getExpiry().isBefore(now));
            Optional<CachedUserResult> optResult = userCache.stream()
                    .filter(entry -> entry.getUsers().stream().allMatch(matchBy))
                    .findFirst();
            return optResult.map(cachedUserResult -> {
                logger.trace("Returning cached result for search: {}", searchHint);
                return cachedUserResult.getUsers();
            }).orElseGet(() -> {
                logger.trace("Computing result for search: {}", searchHint);
                List<IUser> users = getBy.get();
                CachedUserResult result =
                        new CachedUserResult(users, LocalDateTime.now().plusSeconds(USR_CACHE_TTL));
                userCache.add(result);
                return result.getUsers();
            });
        }
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
