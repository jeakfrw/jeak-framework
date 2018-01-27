package de.fearnixx.t3.service.perms;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.service.perms.catalogued.CataloguedTS3Permission;
import de.fearnixx.t3.service.perms.catalogued.TS3Permission;
import de.fearnixx.t3.service.perms.permission.IPermission;
import de.fearnixx.t3.service.perms.permission.IPermissionEntry;
import de.fearnixx.t3.service.perms.permission.PermSourceType;
import de.fearnixx.t3.service.perms.permission.TS3PermissionEntry;
import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.query.IQueryRequest;
import de.fearnixx.t3.ts3.query.PromisedRequest;
import de.fearnixx.t3.ts3.query.QueryConnection;
import de.mlessmann.logging.ILogReceiver;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public class TS3PermissionProvider implements IPermProvider {

    private static final Object classLock = new Object();
    private static Map<String, TS3Permission> cataloguedPerms;

    private ILogReceiver log;
    private QueryConnection connection;

    private final Object lock = new Object();
    private final BiMap<String, Integer> permIDCache;

    public TS3PermissionProvider(QueryConnection connection, ILogReceiver log) {
        this.log = log;
        this.connection = connection;
        permIDCache = HashBiMap.create();

        synchronized (classLock) {
            // Scan for created TS3 permissions just due to laziness of manually listing them here
            // We only need to run the scan once per runtime.
            if (cataloguedPerms == null) {
                cataloguedPerms = new HashMap<>();

                ClassLoader loader = this.getClass().getClassLoader();
                ConfigurationBuilder builder = new ConfigurationBuilder();
                Reflections ref = new Reflections(builder);
                Set<Class<?>> results = ref.getTypesAnnotatedWith(CataloguedTS3Permission.class);
                Map<String, TS3Permission> fCataloguedPerms = cataloguedPerms;
                results.stream()
                       .filter(TS3Permission.class::isAssignableFrom)
                       .forEach(c -> {
                           try {
                               Object perm = c.newInstance();
                               fCataloguedPerms.put(
                                   ((TS3Permission) perm).getInternalSID(),
                                   ((TS3Permission) perm)
                               );
                           } catch (Exception e) {
                               throw new RuntimeException("Cannot create CataloguedTS3Permission!", e);
                           }
                       });
                cataloguedPerms = Collections.unmodifiableMap(cataloguedPerms);
            }
        }
    }

    @Override
    public Optional<IPermission> getPermission(String systemSID) {
        return Optional.ofNullable(cataloguedPerms.getOrDefault(systemSID, null));
    }

    @Override
    public Optional<IPermissionEntry> getEffectivePermission(String systemSID, Integer target, PermSourceType type, Integer channelID) {
        List<IPermissionEntry> context = getEffectivePermissionContext(systemSID, target, type, channelID);
        return Optional.empty();
    }

    @Override
    public List<IPermissionEntry> getEffectivePermissionContext(String systemSID, Integer target, PermSourceType type, Integer channelID) {
        if (systemSID == null) return Collections.emptyList();
        if (!getPermission(systemSID).isPresent()) return Collections.emptyList();
        Integer permNumID = getNumericalPermID(systemSID);
        if (permNumID < 0) return Collections.emptyList();

        List<IPermissionEntry> entries = new ArrayList<>();
        Optional<IPermissionEntry> optEntry;

        if (type == PermSourceType.CLIENT) {
            // TARGET CLIENT
            optEntry = getClientPerm(permNumID, target);
            if (!optEntry.isPresent()) return Collections.emptyList();
            entries.add(optEntry.get());

            PromisedRequest promise = promiseRequest(IQueryRequest.builder()
                                                                  .command("servergroupsbyclient")
                                                                  .addKey("cldbid", target)
                                                                  .build());
            if (!promise.isDone()) {
                log.warning("Failed to retrieve server groups of client!");
                return Collections.emptyList();
            }

            for (IQueryMessageObject object : promise.getAnswer().getObjects()) {
                Integer groupID = Integer.parseInt(object.getProperty("sgid").get());
                optEntry = getServerGroupPerm(permNumID, groupID);
                optEntry.ifPresent(entries::add);
            }

            if (channelID != null) {
                promise = promiseRequest(IQueryRequest.builder()
                                                      .command("channelgroupclientlist")
                                                      .addKey("cldbid", target)
                                                      .addKey("cid", channelID)
                                                      .build());
                if (!promise.isDone()) {
                    log.warning("Failed to retrieve channel groups for cldbid: ", target);
                } else {
                    Integer cgID = Integer.parseInt(promise.getAnswer().getObjects().get(0).getProperty("cgid").get());
                    optEntry = getChannelGroupPerm(permNumID, cgID);
                    optEntry.ifPresent(entries::add);
                }

                optEntry = getChannelPerm(permNumID, channelID);
                optEntry.ifPresent(entries::add);

                optEntry = getChannelClientPerm(permNumID, target, channelID);
                optEntry.ifPresent(entries::add);
            }

        } else if (type == PermSourceType.SERVER_GROUP) {
            // TARGET SERVER GROUP
            optEntry = getServerGroupPerm(permNumID, target);
            optEntry.ifPresent(entries::add);

        } else if (type == PermSourceType.CHANNEL_GROUP) {
            // TARGET CHANNEL GROUP
            optEntry = getChannelGroupPerm(permNumID, target);
            optEntry.ifPresent(entries::add);

        } else if (type == PermSourceType.CHANNEL_CLIENT) {
            // TARGET CHANNEL_CLIENT
            optEntry = getChannelClientPerm(permNumID, target, channelID);
            optEntry.ifPresent(entries::add);

        } else if (type == PermSourceType.CHANNEL) {
            // TARGET CHANNEL
            optEntry = getChannelPerm(permNumID, target);
            optEntry.ifPresent(entries::add);
        }
        return entries;
    }

    protected Integer getNumericalPermID(String systemSID) {
        synchronized (lock) {
            Integer systemID = permIDCache.getOrDefault(systemSID, null);
            if (systemID == null) {
                IQueryEvent.IAnswer resp = promiseRequest(
                    IQueryRequest.builder()
                                 .command("permidgetbyname")
                                 .addKey("permsid", systemSID)
                                 .build()
                ).getAnswer();
                if (resp == null) {
                    log.warning("Failed to retrieve numerical ID for perm: ", systemSID, " - Timed out");
                    return -1;
                }
                if (resp.getError().getID() != 0)
                    throw new RuntimeException("Failed to lookup Permission " + systemSID,
                        new IllegalStateException("Response code is not OK: "
                                                  + resp.getError().getID() + " - "
                                                  + resp.getError().getMessage()));
                Integer id = Integer.parseInt(resp.getObjects().get(0).getProperty("permid").get());
                permIDCache.put(systemSID, id);
                return id;
            }
        }
        return -1;
    }

    protected Optional<IPermissionEntry> getServerGroupPerm(Integer permID, Integer groupID) {
        IQueryRequest req = IQueryRequest.builder()
                                         .command("servergrouppermlist")
                                         .addKey("sgid", groupID)
                                         .build();
        PromisedRequest promise = promiseRequest(req);
        if (!promise.isDone()) {
            log.warning("Failed to retrieve server group perm ", permID);
            return Optional.empty();
        }
        return filterPermEntryFromList(promise.getAnswer(), permID, PermSourceType.SERVER_GROUP);
    }

    protected Optional<IPermissionEntry> getChannelGroupPerm(Integer permID, Integer groupID) {
        IQueryRequest req = IQueryRequest.builder()
                     .command("channelgrouppermlist")
                     .addKey("cgid", groupID)
                     .build();
        PromisedRequest promise = promiseRequest(req);
        if (!promise.isDone()) {
            log.warning("Failed to retrieve channel group perm ", permID);
            return Optional.empty();
        }
        return filterPermEntryFromList(promise.getAnswer(), permID, PermSourceType.CHANNEL_GROUP);
    }

    protected Optional<IPermissionEntry> getChannelPerm(Integer permID, Integer channelID) {
        IQueryRequest req = IQueryRequest.builder()
                                         .command("channelpermlist")
                                         .addKey("cid", channelID)
                                         .build();
        PromisedRequest promise = promiseRequest(req);
        if (!promise.isDone()) {
            log.warning("Failed to retrieve channel perm ", permID);
            return Optional.empty();
        }
        return filterPermEntryFromList(promise.getAnswer(), permID, PermSourceType.CHANNEL);
    }

    protected Optional<IPermissionEntry> getClientPerm(Integer permID, Integer clientID) {
        IQueryRequest req = IQueryRequest.builder()
                                         .command("clientpermlist")
                                         .addKey("clid", clientID)
                                         .build();
        PromisedRequest promise = promiseRequest(req);
        if (!promise.isDone()) {
            log.warning("Failed to retrieve client perm ", permID);
            return Optional.empty();
        }
        return filterPermEntryFromList(promise.getAnswer(), permID, PermSourceType.CLIENT);
    }

    protected Optional<IPermissionEntry> getChannelClientPerm(Integer permID, Integer clientDBID, Integer channelID) {
        IQueryRequest req = IQueryRequest.builder()
                                         .command("channelclientpermlist")
                                         .addKey("cid", channelID)
                                         .addKey("clid", clientDBID)
                                         .build();
        PromisedRequest promise = promiseRequest(req);
        if (!promise.isDone()) {
            log.warning("Failed to retrieve channel client perm ", permID);
            return Optional.empty();
        }
        return filterPermEntryFromList(promise.getAnswer(), permID, PermSourceType.CHANNEL_CLIENT);
    }

    protected PromisedRequest promiseRequest(IQueryRequest req) {
        PromisedRequest promise = connection.promiseRequest(req);
        int tries = 0;
        while (tries++ < 3 && !promise.isDone()) {
            promise.get(1, TimeUnit.SECONDS);
        }
        return promise;
    }

    protected Optional<IPermissionEntry> filterPermEntryFromList(IQueryEvent.IAnswer event, Integer permID, PermSourceType type) {

        for (IQueryMessageObject object : event.getObjects()) {

            if (permID.toString().equals(object.getProperty(PropertyKeys.Permission.ID).get())
                || permID.toString().equals(object.getProperty(PropertyKeys.Permission.ID_SHORT).get())) {

                String sid = permIDCache.inverse().get(permID);
                IPermission perm = cataloguedPerms.getOrDefault(sid, null);
                if (perm == null) {
                    log.warning("Failed reverse-lookup for: " + sid + " is it catalogued?");
                }
                IPermissionEntry entry = new TS3PermissionEntry(perm, object, type);
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }
}
