package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.IServiceManager;
import de.fearnixx.jeak.service.database.IDatabaseService;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.permission.teamspeak.AbstractTS3PermissionProvider;
import de.fearnixx.jeak.service.permission.teamspeak.DBPermissionProvider;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3PermissionProvider;
import de.fearnixx.jeak.service.permission.teamspeak.QueryPermissionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkService(serviceInterface = IPermissionService.class)
public class PermissionService implements IPermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final Map<String, IPermissionProvider> providers = new ConcurrentHashMap<>();
    public static final String PERSISTENCE_UNIT_NAME = "teamspeak";

    @Inject
    private IDatabaseService databaseService;

    @Inject
    private IInjectionService injectionService;

    @Inject
    private IServiceManager serviceManager;

    @Inject
    private IEventService eventService;

    @Listener(order = Listener.Orders.SYSTEM)
    public void onPreInitialize(IBotStateEvent.IPreInitializeEvent event) {
        AbstractTS3PermissionProvider provider;

        Optional<IPersistenceUnit> optPersistenceUnit = databaseService.getPersistenceUnit(PERSISTENCE_UNIT_NAME);
        if (optPersistenceUnit.isPresent()) {
            logger.info("Persistence unit available! Using faster db-supported algorithm.");
            provider = new DBPermissionProvider();

        } else  {
            logger.warn("Persistence unit not available. Expect degraded performance in permission-checks.");
            logger.info("Please consider registering persistence unit \"{}\" to enable direct database access.", PERSISTENCE_UNIT_NAME);
            provider = new QueryPermissionProvider();
        }

        injectionService.injectInto(provider);
        eventService.registerListener(provider);
        serviceManager.registerService(ITS3PermissionProvider.class, provider);
        registerProvider(IUserIdentity.SERVICE_TEAMSPEAK, provider);
    }

    @Override
    public Optional<IPermissionProvider> provide(String systemID) {
        return Optional.ofNullable(providers.getOrDefault(systemID, null));
    }

    @Override
    public void registerProvider(String systemID, IPermissionProvider provider) {
        Objects.requireNonNull(provider, "Permission provider may not be null!");

        IPermissionProvider existing = providers.put(systemID, provider);;
        if (existing != null) {
            logger.info("Replaced permission provider for sID \"{}\": {} with {}",
                    systemID, existing.getClass().getName(), provider.getClass().getName());
        }
    }

    @Override
    public ITS3PermissionProvider getTS3Provider() {
        return (ITS3PermissionProvider) provide(IUserIdentity.SERVICE_TEAMSPEAK)
                .orElseThrow(() -> new IllegalStateException("TeamSpeak permission provider not registered!"));
    }

    @Override
    public IPermissionProvider getFrameworkProvider() {
        return provide("jeak")
                .orElseThrow(() -> new IllegalStateException("Framework permission provider not registered!"));
    }
}
